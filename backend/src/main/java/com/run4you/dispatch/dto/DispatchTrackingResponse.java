package com.run4you.dispatch.dto;

import com.run4you.dispatch.entity.DispatchStatusHistory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 점주 실시간 추적 화면(지도/ETA/타임라인)용 조회 응답.
 */
public record DispatchTrackingResponse(
        Long assignmentId,
        Long asRequestId,
        String currentStatus,
        Integer etaMinutes,
        BigDecimal latitude,
        BigDecimal longitude,
        LocalDateTime lastChangedAt,
        List<TimelineItem> timeline
) {
    public record TimelineItem(
            String status,
            Integer etaMinutes,
            BigDecimal latitude,
            BigDecimal longitude,
            LocalDateTime changedAt
    ) {
        public static TimelineItem from(DispatchStatusHistory h) {
            return new TimelineItem(
                    h.getStatus().name(), h.getEtaMinutes(),
                    h.getLatitude(), h.getLongitude(), h.getChangedAt());
        }
    }
}
