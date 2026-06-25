package com.run4you.report.dto;

import com.run4you.report.entity.RepairReport;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 정비 리포트 응답.
 * 부품비·공임비·합계와 부품별 단가 검증 결과를 함께 내려준다.
 */
public record ReportResponse(
        Long id,
        Long assignmentId,
        Long asRequestId,
        Long engineerId,
        Long equipmentId,
        BigDecimal laborCost,
        BigDecimal partsCost,
        BigDecimal totalCost,
        String diagnosis,
        boolean hasPriceMismatch,
        List<String> mismatchMessages,
        List<PartLineResponse> parts,
        LocalDateTime createdAt
) {
    public static ReportResponse from(RepairReport r) {
        List<PartLineResponse> lines = r.getParts().stream()
                .map(PartLineResponse::from)
                .toList();

        List<String> mismatches = r.getParts().stream()
                .filter(p -> !p.isPriceMatched())
                .map(p -> String.format("%s 청구단가 %,.0f원 ≠ 표준단가 %,.0f원",
                        p.getPartCode(), p.getAppliedPrice(), p.getStandardPrice()))
                .toList();

        return new ReportResponse(
                r.getId(),
                r.getAssignmentId(),
                r.getAsRequestId(),
                r.getEngineerId(),
                r.getEquipmentId(),
                r.getLaborCost(),
                r.getPartsCost(),
                r.getTotalCost(),
                r.getDiagnosis(),
                !mismatches.isEmpty(),
                mismatches,
                lines,
                r.getCreatedAt()
        );
    }
}
