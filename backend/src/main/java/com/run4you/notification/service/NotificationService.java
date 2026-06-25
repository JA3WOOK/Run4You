package com.run4you.notification.service;

import com.run4you.dispatch.domain.DispatchStatus;
import com.run4you.dispatch.dto.DispatchEventPayload;
import com.run4you.dispatch.port.AssignmentGateway.AssignmentView;
import com.run4you.notification.dto.NotificationResponse;
import com.run4you.notification.entity.Notification;
import com.run4you.notification.entity.NotificationType;
import com.run4you.notification.repository.NotificationRepository;
import com.run4you.notification.sse.SseEmitterRepository;
import com.run4you.notification.sse.SsePushService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    /** SSE 타임아웃 30분 (브라우저 EventSource 가 onerror 시 자동 재연결) */
    private static final long SSE_TIMEOUT_MS = 30 * 60 * 1000L;

    private final NotificationRepository notificationRepository;
    private final SseEmitterRepository emitterRepository;
    private final SsePushService ssePushService;

    // ── 구독 ───────────────────────────────────────────────────────────

    /** 현재 사용자(점주/관제진) 실시간 알림 구독 */
    public SseEmitter subscribe(Long userId) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);
        emitterRepository.add(userId, emitter);
        try {
            // 최초 연결 확인 이벤트(프록시 타임아웃 방지 + 클라 onopen 트리거)
            emitter.send(SseEmitter.event().name("connected").data("subscribed:" + userId));
        } catch (IOException e) {
            emitter.completeWithError(e);
        }
        return emitter;
    }

    // ── 출동 이벤트 → 알림 영속 + push ─────────────────────────────────

    /**
     * 출동 상태 변경을 점주 알림으로 기록하고 실시간 push 한다.
     * (별도 트랜잭션 — 코어 상태 전이 트랜잭션과 분리해, 알림 실패가 전이를 롤백하지 않도록 한다.)
     */
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public void notifyDispatchEvent(AssignmentView view, DispatchStatus status, DispatchEventPayload payload) {
        Optional<Copy> copy = messageOf(status);
        if (copy.isEmpty()) {
            return; // 점주 알림 대상이 아닌 상태(PENDING_ACCEPT/CANCELLED 등)
        }
        Notification saved = notificationRepository.save(
                Notification.builder()
                        .recipientId(view.requesterId())
                        .asRequestId(view.asRequestId())
                        .type(copy.get().type())
                        .title(copy.get().title())
                        .message(copy.get().message())
                        .build());

        ssePushService.push(view.requesterId(), "notification", NotificationResponse.from(saved));
    }

    /** 상태 → 알림 유형/문구 매핑 (기획안 §19.1) */
    private Optional<Copy> messageOf(DispatchStatus status) {
        return switch (status) {
            case ACCEPTED  -> Optional.of(new Copy(NotificationType.ASSIGNED,  "엔지니어 배정 완료", "엔지니어가 배정되었습니다."));
            case DISPATCHED-> Optional.of(new Copy(NotificationType.DISPATCHED, "출동 시작",        "엔지니어가 출발했습니다."));
            case ARRIVED   -> Optional.of(new Copy(NotificationType.ARRIVED,    "현장 도착",        "엔지니어가 현장에 도착했습니다."));
            case REPAIRING -> Optional.of(new Copy(NotificationType.REPAIRING,  "수리 개시",        "수리를 시작했습니다."));
            case COMPLETED -> Optional.of(new Copy(NotificationType.COMPLETED,  "수리 완료",        "수리가 완료되었습니다."));
            case PENDING_ACCEPT, CANCELLED -> Optional.empty();
        };
    }

    private record Copy(NotificationType type, String title, String message) {}

    // ── 조회/읽음 ─────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<NotificationResponse> listMine(Long userId, int limit) {
        return notificationRepository
                .findByRecipientIdOrderByCreatedAtDesc(userId, PageRequest.of(0, limit))
                .stream().map(NotificationResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public long unreadCount(Long userId) {
        return notificationRepository.countByRecipientIdAndReadFalse(userId);
    }

    @Transactional
    public void markRead(Long userId, Long notificationId) {
        Notification n = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("알림을 찾을 수 없습니다: " + notificationId));
        if (!n.getRecipientId().equals(userId)) {
            throw new org.springframework.security.access.AccessDeniedException("본인 알림만 처리할 수 있습니다.");
        }
        n.markRead();
    }

    @Transactional
    public void markAllRead(Long userId) {
        notificationRepository.markAllRead(userId);
    }
}
