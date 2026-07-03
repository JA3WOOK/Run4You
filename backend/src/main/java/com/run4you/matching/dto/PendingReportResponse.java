package com.run4you.matching.dto;

import com.run4you.matching.entity.Assignment;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
public class PendingReportResponse {
    private Long assignmentId;
    private Long asRequestId;
    private Long equipmentId;
    private Long engineerId;
    private String equipmentName;
    private String storeName;
    private LocalDateTime completedAt;

    public static PendingReportResponse of(Assignment a) {
        var req = a.getAsRequest();
        var eq  = req.getEquipment();
        return PendingReportResponse.builder()
                .assignmentId(a.getId())
                .asRequestId(req.getId())
                .equipmentId(eq.getId())
                .engineerId(a.getEngineer().getId())
                .equipmentName(eq.getName())
                .storeName(req.getStore().getName())
                .completedAt(a.getCompletedAt())
                .build();
    }
}