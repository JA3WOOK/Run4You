package com.run4you.matching.dto;

import com.run4you.matching.entity.Assignment;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ActiveAssignmentResponse {
    private Long assignmentId;
    private Long asRequestId;
    private Long equipmentId;
    private Long engineerId;
    private String status; // ACCEPTED / DISPATCHED / ARRIVED / REPAIRING

    public static ActiveAssignmentResponse of(Assignment a) {
        return ActiveAssignmentResponse.builder()
                .assignmentId(a.getId())
                .asRequestId(a.getAsRequest().getId())
                .equipmentId(a.getAsRequest().getEquipment().getId())
                .engineerId(a.getEngineer().getId())
                .status(a.getStatus().name())
                .build();
    }
}