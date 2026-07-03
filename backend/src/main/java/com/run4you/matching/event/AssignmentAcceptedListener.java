package com.run4you.matching.event;

import com.run4you.dispatch.domain.DispatchStatus;
import com.run4you.dispatch.dto.DispatchEventPayload;
import com.run4you.dispatch.port.AssignmentGateway.AssignmentView;
import com.run4you.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 배정 확정(수락) 이벤트를 커밋 이후에 받아 점주·엔지니어·본사관리자에게 알림을 전송한다.
 *
 * <p>내 도메인④의 출동 상태전이 알림과 동일하게 {@code NotificationService.notifyDispatchEvent}
 * 를 재사용하되, status 를 {@link DispatchStatus#ACCEPTED} 로 넘겨 "배정 완료" 문구가 나가도록 한다.
 * 이로써 점주 홈의 진행단계(접수→배정)가 SSE 로 새로고침 없이 실시간 갱신된다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AssignmentAcceptedListener {

    private final NotificationService notificationService;

    /**
     * 커밋 이후 실행. 알림 전송 실패가 수락 트랜잭션에 영향을 주지 않도록 REQUIRES_NEW + try/catch.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onAssignmentAccepted(AssignmentAcceptedEvent e) {
        try {
            AssignmentView view = new AssignmentView(
                    e.assignmentId(),
                    e.asRequestId(),
                    e.engineerId(),
                    e.requesterId(),
                    e.storeId(),
                    e.brandId(),
                    DispatchStatus.ACCEPTED
            );

            // 배정 시점 payload — 위치/ETA 는 아직 없음(출동 전)이므로 null
            DispatchEventPayload payload = new DispatchEventPayload(
                    e.assignmentId(),
                    e.asRequestId(),
                    DispatchStatus.ACCEPTED.name(),
                    null,
                    null,
                    null,
                    LocalDateTime.now()
            );

            notificationService.notifyDispatchEvent(view, DispatchStatus.ACCEPTED, payload);
        } catch (Exception ex) {
            // 알림 실패는 배정 자체를 되돌리지 않는다 (이미 커밋됨)
            log.warn("[배정알림] 발송 실패 — assignmentId={}, cause={}", e.assignmentId(), ex.getMessage());
        }
    }
}