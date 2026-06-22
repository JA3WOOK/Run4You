package com.run4you.asrequest.dto;

import com.run4you.asrequest.entity.AsStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder

// 진단서 및 영수증 목록
public class ReceiptListResponseDto {

    // 전체 목록
    private List<ReceiptItemDto> receipts;

    // 목록 1건
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReceiptItemDto {
        private Long id;
        private LocalDateTime requestedAt; // 접수 시각
        private AsStatus status;
        private String equipmentName; // equipment
        private String modelName; // equipment
        private String diagnosis; // 고장 원인 - repair_reports
        private String engineerName; // users
        private LocalDateTime startTime; // assignments
        private LocalDateTime endTime; // assignments
        private BigDecimal totalCost; // repair_reports
    }
}
