package com.run4you.notification.sse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Collection;

/**
 * 등록된 Emitter 로 이벤트를 안전하게 전송한다.
 * 전송 실패(클라이언트 끊김) 시 해당 Emitter 를 정리한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SsePushService {

    private final SseEmitterRepository repository;

    /** 단일 사용자에게 전송 */
    public void push(Long userId, String eventName, Object data) {
        for (SseEmitter emitter : repository.get(userId)) {
            sendSafe(userId, emitter, eventName, data);
        }
    }

    /** 다중 사용자(점주 + 관제진)에게 전송 */
    public void pushToAll(Collection<Long> userIds, String eventName, Object data) {
        for (Long userId : userIds) {
            push(userId, eventName, data);
        }
    }

    /**
     * 모든 구독자에게 하트비트(주석 라인 ":ping")를 전송한다.
     * 데이터 이벤트가 아니라 SSE 주석이라 클라이언트 핸들러를 트리거하지 않으며,
     * L7 프록시/로드밸런서의 유휴 타임아웃으로 연결이 끊기는 것을 막는다.
     */
    public void heartbeatAll() {
        for (Long userId : repository.userIds()) {
            for (SseEmitter emitter : repository.get(userId)) {
                try {
                    emitter.send(SseEmitter.event().comment("ping"));
                } catch (IOException | IllegalStateException e) {
                    repository.remove(userId, emitter);
                }
            }
        }
    }

    private void sendSafe(Long userId, SseEmitter emitter, String eventName, Object data) {
        try {
            emitter.send(SseEmitter.event().name(eventName).data(data));
        } catch (IOException | IllegalStateException e) {
            // 연결 끊김/완료된 Emitter — 정리
            repository.remove(userId, emitter);
            log.debug("[SSE] 전송 실패로 Emitter 제거 userId={}, reason={}", userId, e.getMessage());
        }
    }
}
