package com.run4you.report.dto;

import com.run4you.report.entity.RepairReportParts;

import java.math.BigDecimal;

/** 정비 리포트 응답의 부품 한 줄 */
public record PartLineResponse(
        Long partId,
        String partCode,
        String partName,
        int quantity,
        BigDecimal appliedPrice,
        BigDecimal standardPrice,
        boolean priceMatched,
        BigDecimal lineTotal
) {
    public static PartLineResponse from(RepairReportParts p) {
        return new PartLineResponse(
                p.getPartId(),
                p.getPartCode(),
                p.getPartName(),
                p.getQuantity(),
                p.getAppliedPrice(),
                p.getStandardPrice(),
                p.isPriceMatched(),
                p.getLineTotal()
        );
    }
}
