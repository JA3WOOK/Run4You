package com.run4you.dispatch.dto;

import com.run4you.dispatch.domain.DispatchStatus;
import com.run4you.dispatch.entity.DispatchStatusHistory;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * SSE 로 점주·관제 센터에 전송되는 실시간 출동 이벤트. (기획안 §19.2)
 */
public record DispatchEventPayload(
        Long assignmentId,
        Long asRequestId,
        String status,
        Integer etaMinutes,
        BigDecimal latitude,
        BigDecimal longitude,
        LocalDateTime changedAt
) {
    public static DispatchEventPayload of(Long asRequestId, DispatchStatusHistory h) {
        return new DispatchEventPayload(
                h.getAssignmentId(),
                asRequestId,
                h.getStatus().name(),
                h.getEtaMinutes(),
                h.getLatitude(),
                h.getLongitude(),
                h.getChangedAt()
        );
    }

    public DispatchStatus statusEnum() {
        return DispatchStatus.valueOf(status);
    }
}
