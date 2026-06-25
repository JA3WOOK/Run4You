package com.run4you.notification.sse;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 주기적으로 모든 SSE 구독자에게 하트비트를 전송한다.
 * 유휴 연결이 프록시/LB 타임아웃으로 끊기는 것을 방지하고, 끊긴 Emitter 를 정리한다.
 */
@Component
@RequiredArgsConstructor
public class SseHeartbeatScheduler {

    private final SsePushService ssePushService;

    @Scheduled(fixedRateString = "${dispatch.sse.heartbeat-ms:25000}")
    public void heartbeat() {
        ssePushService.heartbeatAll();
    }
}
