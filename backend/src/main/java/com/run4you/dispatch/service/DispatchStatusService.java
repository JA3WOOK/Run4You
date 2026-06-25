package com.run4you.dispatch.service;

import com.run4you.dispatch.domain.DispatchStatus;
import com.run4you.dispatch.dto.DispatchEventPayload;
import com.run4you.dispatch.dto.DispatchStatusUpdateRequest;
import com.run4you.dispatch.dto.DispatchTrackingResponse;
import com.run4you.dispatch.dto.EngineerLocationPing;
import com.run4you.dispatch.entity.DispatchStatusHistory;
import com.run4you.dispatch.exception.DispatchException;
import com.run4you.dispatch.port.AssignmentGateway;
import com.run4you.dispatch.port.AssignmentGateway.AssignmentView;
import com.run4you.dispatch.port.LocationGateway;
import com.run4you.dispatch.port.LocationGateway.GeoPoint;
import com.run4you.dispatch.repository.DispatchStatusHistoryRepository;
import com.run4you.dispatch.sse.DispatchSsePublisher;
import com.run4you.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 출동 상태머신 핵심 서비스.
 *
 * <p>전이 검증 → ETA 산출 → 이력 적재 → assignments/as_requests 동기화 →
 * 커밋 후 SSE/알림 발행 순으로 처리한다.
 *
 * <p>SSE·알림 발행은 트랜잭션 커밋 이후(AFTER_COMMIT)에 수행해, DB 반영이 확정된 뒤에만
 * 클라이언트로 push 되도록 한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DispatchStatusService {

    private final AssignmentGateway assignmentGateway;
    private final LocationGateway locationGateway;
    private final EtaCalculator etaCalculator;
    private final DispatchStatusHistoryRepository historyRepository;
    private final ApplicationEventPublisher eventPublisher;

    private final DispatchSsePublisher ssePublisher;
    private final NotificationService notificationService;

    /**
     * 엔지니어의 출동 상태 변경.
     *
     * @param assignmentId    배정 ID
     * @param actingEngineerId 요청 주체(인증된 엔지니어) user_id
     */
    @Transactional
    public DispatchEventPayload updateStatus(Long assignmentId, Long actingEngineerId,
                                             DispatchStatusUpdateRequest req) {
        AssignmentView view = assignmentGateway.getAssignment(assignmentId);
        if (view == null) {
            throw DispatchException.assignmentNotFound(assignmentId);
        }

        // 1) 권한: 본인에게 배정된 건만 변경 가능
        if (!view.engineerId().equals(actingEngineerId)) {
            throw DispatchException.forbidden("본인에게 배정된 건만 상태를 변경할 수 있습니다.");
        }

        DispatchStatus current = view.currentStatus();
        DispatchStatus target = req.status();

        // 2) 상태머신 전이 검증
        if (!current.canTransitionTo(target)) {
            throw DispatchException.invalidTransition(current, target);
        }
        // (확장) REPAIRING 진입 교육 이수 게이트는 LMS(⑥) 연동 시 이 지점에 추가 — MVP 미적용

        // 3) 간이 ETA 산출 (출동 중일 때만 의미, 도착 이후 0)
        Integer eta = resolveEta(view, target, req);

        // 4) 이력 적재
        LocalDateTime now = LocalDateTime.now();
        DispatchStatusHistory history = historyRepository.save(
                DispatchStatusHistory.builder()
                        .assignmentId(assignmentId)
                        .status(target)
                        .latitude(req.latitude())
                        .longitude(req.longitude())
                        .etaMinutes(eta)
                        .changedAt(now)
                        .build());

        // 5) 코어 상태 동기화 (assignments + as_requests)
        assignmentGateway.applyDispatchStatus(assignmentId, target, now);

        DispatchEventPayload payload = DispatchEventPayload.of(view.asRequestId(), history);
        log.info("[Dispatch] assignment={} {} -> {} (eta={}min)", assignmentId, current, target, eta);

        // 6) 커밋 후 발행 (AFTER_COMMIT)
        eventPublisher.publishEvent(new StatusChangedEvent(view, target, payload));
        return payload;
    }

    private Integer resolveEta(AssignmentView view, DispatchStatus target, DispatchStatusUpdateRequest req) {
        if (target.isOnSiteOrLater()) {
            return 0; // 도착/수리/완료 단계는 ETA 0
        }
        if (!target.isEnRoute()) {
            return null; // ACCEPTED 등은 ETA 미산출
        }
        GeoPoint store = locationGateway.storeLocation(view.storeId()).orElse(null);
        // 요청에 실린 현재 좌표 우선, 없으면 프로필 위치로 폴백
        if (req.latitude() != null && req.longitude() != null) {
            return etaCalculator.etaMinutes(req.latitude(), req.longitude(), store);
        }
        GeoPoint engineer = locationGateway.engineerLocation(view.engineerId()).orElse(null);
        return etaCalculator.etaMinutes(engineer, store);
    }

    // ── 점주 추적 조회 ────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public DispatchTrackingResponse getTracking(Long assignmentId, Long requesterId) {
        AssignmentView view = assignmentGateway.getAssignment(assignmentId);
        if (view == null) {
            throw DispatchException.assignmentNotFound(assignmentId);
        }
        // 점주 본인 접수 건만 조회 허용 (관제진은 별도 관리자 API 사용)
        if (!view.requesterId().equals(requesterId)) {
            throw DispatchException.forbidden("본인 매장의 접수 건만 조회할 수 있습니다.");
        }

        List<DispatchStatusHistory> timeline =
                historyRepository.findByAssignmentIdOrderByChangedAtAsc(assignmentId);
        DispatchStatusHistory latest = historyRepository
                .findFirstByAssignmentIdOrderByChangedAtDesc(assignmentId).orElse(null);

        return new DispatchTrackingResponse(
                assignmentId,
                view.asRequestId(),
                latest != null ? latest.getStatus().name() : view.currentStatus().name(),
                latest != null ? latest.getEtaMinutes() : null,
                latest != null ? latest.getLatitude() : null,
                latest != null ? latest.getLongitude() : null,
                latest != null ? latest.getChangedAt() : null,
                timeline.stream().map(DispatchTrackingResponse.TimelineItem::from).toList()
        );
    }

    // ── 출동 중 실시간 위치/ETA 갱신 ──────────────────────────────────

    /**
     * 상태 변경 없이 엔지니어 현재 좌표만 갱신해 ETA 를 재산출하고 지도용 SSE 를 발행한다.
     * 이력 테이블 적재 없음(전이가 아닌 일시적 추적 데이터) — 점주 지도가 실시간으로 움직이도록 한다.
     */
    @Transactional(readOnly = true)
    public DispatchEventPayload updateEngineerLocation(Long assignmentId, Long actingEngineerId,
                                                       EngineerLocationPing ping) {
        AssignmentView view = assignmentGateway.getAssignment(assignmentId);
        if (view == null) {
            throw DispatchException.assignmentNotFound(assignmentId);
        }
        if (!view.engineerId().equals(actingEngineerId)) {
            throw DispatchException.forbidden("본인에게 배정된 건만 위치를 갱신할 수 있습니다.");
        }

        GeoPoint store = locationGateway.storeLocation(view.storeId()).orElse(null);
        Integer eta = view.currentStatus().isEnRoute()
                ? etaCalculator.etaMinutes(ping.latitude(), ping.longitude(), store)
                : (view.currentStatus().isOnSiteOrLater() ? 0 : null);

        DispatchEventPayload payload = new DispatchEventPayload(
                assignmentId, view.asRequestId(), view.currentStatus().name(),
                eta, ping.latitude(), ping.longitude(), LocalDateTime.now());

        ssePublisher.publishLocation(view, payload); // DB 쓰기 없음 → 즉시 발행
        return payload;
    }

    // ── 커밋 후 발행 이벤트 ───────────────────────────────────────────

    private record StatusChangedEvent(AssignmentView view, DispatchStatus status, DispatchEventPayload payload) {}

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    void onStatusChanged(StatusChangedEvent e) {
        ssePublisher.publish(e.view(), e.payload());                       // 점주 + 관제 스트림
        notificationService.notifyDispatchEvent(e.view(), e.status(), e.payload()); // 점주 알림 영속+push
    }
}
