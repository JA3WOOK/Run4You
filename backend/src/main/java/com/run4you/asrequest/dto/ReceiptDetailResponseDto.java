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

// 진단서 및 영수증 상세 목록
public class ReceiptDetailResponseDto {

    // 접수 기본 정보
    private Long asRequestId;
    private String invoiceNumber; // settlements - 영수증 번호
    private String pdfUrl;  // settlements - 영수증 + 진단서 합본 PDF 경로
    private AsStatus status;

    // 진단서 영역
    private String equipmentName; // equipment
    private String modelName;  // equipment
    private String engineerName;  // users
    private BigDecimal engineerRating; // engineer_profiles
    private LocalDateTime startTime;  // dispatch_status_history
    private LocalDateTime endTime;  // assignments
    private String diagnosis;  // repair_reports

    // 교체 부품 목록
    private List<PartItemDto> parts;

    // 금액 요약
    private BigDecimal laborCost;  // repair_reports - 공임비
    private BigDecimal partsCost;  // repair_reports - 부품비 합계
    private BigDecimal commissionAmount; // settlements - 긴급 수수료
    private BigDecimal vatAmount;  // settlements - 부가세
    private BigDecimal billedAmount;  // settlements - 최종 합계

    // 교체 부품 1건
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PartItemDto {
        private String partCode;  // parts
        private String partName;  // parts
        private Integer quantity;  // repair_report_parts - 수량
        private BigDecimal unitPrice; // repair_report_parts.appliedPrice - 단가
        private BigDecimal amount; // 단가 × 수량 (계산)
    }
}
