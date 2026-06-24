package com.run4you.certificate.dto;

import com.run4you.certificate.entity.HealthCertificate;

import java.time.LocalDateTime;

/**
 * 진단서 응답 — 점수·등급과 산정 근거(4개 지표)를 함께 내려준다.
 */
public record CertificateResponse(
        Long id,
        String certificateNo,
        Long equipmentId,
        Long reportId,
        int healthScore,
        String grade,
        int repairCount,
        int usageYears,
        int replacedPartsCount,
        boolean recentFault,
        LocalDateTime issuedAt
) {
    public static CertificateResponse from(HealthCertificate c) {
        return new CertificateResponse(
                c.getId(),
                c.getCertificateNo(),
                c.getEquipmentId(),
                c.getReportId(),
                c.getHealthScore(),
                c.getGrade().name(),
                c.getRepairCount(),
                c.getUsageYears(),
                c.getReplacedPartsCount(),
                c.isRecentFault(),
                c.getIssuedAt()
        );
    }
}
