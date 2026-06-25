package com.run4you.settlement.dto;

import com.run4you.settlement.entity.Settlement;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 정산 1건 응답 (피그마 정산 관리 표의 한 행).
 */
public record SettlementResponse(
        Long id,
        String invoiceNumber,
        Long reportId,
        Long brandId,
        Long engineerId,
        BigDecimal partsCost,
        BigDecimal laborCost,
        BigDecimal emergencyFee,
        BigDecimal billedAmount,
        BigDecimal vatAmount,
        String verificationStatus,
        String approvalStatus,
        String flagReason,
        Long approvedBy,
        LocalDateTime createdAt
) {
    public static SettlementResponse from(Settlement s) {
        return new SettlementResponse(
                s.getId(),
                s.getInvoiceNumber(),
                s.getReportId(),
                s.getBrandId(),
                s.getEngineerId(),
                s.getPartsCost(),
                s.getLaborCost(),
                s.getEmergencyFee(),
                s.getBilledAmount(),
                s.getVatAmount(),
                s.getVerificationStatus().name(),
                s.getApprovalStatus().name(),
                s.getFlagReason(),
                s.getApprovedBy(),
                s.getCreatedAt()
        );
    }
}
