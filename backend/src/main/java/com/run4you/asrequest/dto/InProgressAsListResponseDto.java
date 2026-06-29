package com.run4you.asrequest.dto;

import com.run4you.dispatch.domain.DispatchStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class InProgressAsListResponseDto {

    private List<InProgressItemDto> requests;
    private int totalCount;

    @Getter
    @Builder
    public static class InProgressItemDto {
        private Long asRequestId;
        private String requestNo;
        private LocalDateTime requestedAt;

        private Long equipmentId;             // 접수 내용 모달용
        private String equipmentName;
        private String modelName;
        private String category;

        private DispatchStatus currentStatus; // 진행단계 → 프론트 stepIndex (nullable: 배정 전)
        private Long assignmentId;            // 실시간 추적용 (nullable)

        private String engineerName;
        private String engineerPhone;

        private Integer etaMinutes;
    }
}