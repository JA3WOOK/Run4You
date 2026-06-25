package com.run4you.dispatch.sse;

import com.run4you.dispatch.dto.DispatchEventPayload;
import com.run4you.dispatch.port.AssignmentGateway.AssignmentView;
import com.run4you.dispatch.port.ControlCenterGateway;
import com.run4you.notification.sse.SsePushService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 출동 상태 이벤트를 SSE 로 발행한다.
 *
 * <p>수신 대상:
 * <ul>
 *   <li>접수자(점주) — 본인 접수 건 실시간 추적</li>
 *   <li>해당 브랜드 관제진(BRAND_ADMIN/SUPER_ADMIN) — 통합 관제 스트림</li>
 * </ul>
 * 대상을 서버에서 선별해 push 하므로 "본인 접수 건만 수신"이 자연히 보장된다.
 */
@Component
@RequiredArgsConstructor
public class DispatchSsePublisher {

    private final SsePushService ssePushService;
    private final ControlCenterGateway controlCenterGateway;

    /** 상태 변경 이벤트 발행 (event: dispatch) */
    public void publish(AssignmentView view, DispatchEventPayload payload) {
        ssePushService.pushToAll(recipients(view), "dispatch", payload);
    }

    /** 출동 중 실시간 위치/ETA 갱신 발행 (event: location) — 상태 변경 없이 지도 갱신용 */
    public void publishLocation(AssignmentView view, DispatchEventPayload payload) {
        ssePushService.pushToAll(recipients(view), "location", payload);
    }

    private Set<Long> recipients(AssignmentView view) {
        Set<Long> recipients = new LinkedHashSet<>();
        recipients.add(view.requesterId());                                  // 점주
        recipients.addAll(controlCenterGateway.controlCenterUserIds(view.brandId())); // 관제진
        return recipients;
    }
}
