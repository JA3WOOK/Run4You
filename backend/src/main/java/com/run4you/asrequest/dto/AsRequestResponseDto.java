package com.run4you.asrequest.dto;

import com.run4you.asrequest.entity.AsStatus;
import com.run4you.asrequest.entity.Priority;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder

// 긴급 A/S 접수 완료 후 응답
public class AsRequestResponseDto {

    private Long id;
    private Priority priority;
    private AsStatus status;
    private LocalDateTime requestedAt;
    private Long equipmentId;        // AsRequest.equipment.id
    private String equipmentName;    // AsRequest.equipment.name
}
