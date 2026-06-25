package com.run4you.settlement.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 정산 (테이블 settlements, 정비 리포트와 1:1).
 * 정산 공식(피그마 기준):
 *   합계(billedAmount) = 부품비 + 공임비 + 긴급수수료
 *   긴급수수료(emergencyFee) = EMERGENCY 면 (부품비+공임비) x 30%, NORMAL 이면 0
 *   VAT(vatAmount) = 합계 x 10% (별도)
 *
 * <p>commissionAmount 는 팀원 영수증 화면이 "긴급수수료"로 읽는 칸이라 emergencyFee 와 동일 값을 채운다.
 */
@Entity
@Table(name = "settlements")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Settlement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "report_id", nullable = false, unique = true)
    private Long reportId;

    @Column(name = "brand_id")
    private Long brandId;

    @Column(name = "engineer_id", nullable = false)
    private Long engineerId;

    @Column(name = "invoice_number", nullable = false, unique = true, length = 30)
    private String invoiceNumber;

    @Column(name = "labor_cost", nullable = false, precision = 12, scale = 2)
    private BigDecimal laborCost;

    @Column(name = "parts_cost", nullable = false, precision = 12, scale = 2)
    private BigDecimal partsCost;

    /** 긴급수수료 = EMERGENCY 시 (부품비+공임비)x30% */
    @Column(name = "emergency_fee", nullable = false, precision = 12, scale = 2)
    private BigDecimal emergencyFee;

    /** 팀원 영수증 호환 칸 — emergencyFee 와 동일 값(긴급수수료) */
    @Column(name = "commission_amount", precision = 12, scale = 2)
    private BigDecimal commissionAmount;

    /** 합계 = 부품비 + 공임비 + 긴급수수료 */
    @Column(name = "billed_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal billedAmount;

    /** 부가세 = 합계 x 10% (별도) */
    @Column(name = "vat_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal vatAmount;

    /** 영수증+진단서 합본 PDF 경로 (추후) */
    @Column(name = "pdf_url", length = 255)
    private String pdfUrl;

    @Column(name = "verification_status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private VerificationStatus verificationStatus;

    @Column(name = "approval_status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ApprovalStatus approvalStatus;

    @Column(name = "flag_reason", length = 255)
    private String flagReason;

    @Column(name = "approved_by")
    private Long approvedBy;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    private Settlement(Long reportId, Long brandId, Long engineerId, String invoiceNumber,
                       BigDecimal laborCost, BigDecimal partsCost, BigDecimal emergencyFee,
                       BigDecimal billedAmount, BigDecimal vatAmount,
                       VerificationStatus verificationStatus, ApprovalStatus approvalStatus,
                       String flagReason) {
        this.reportId = reportId;
        this.brandId = brandId;
        this.engineerId = engineerId;
        this.invoiceNumber = invoiceNumber;
        this.laborCost = laborCost;
        this.partsCost = partsCost;
        this.emergencyFee = emergencyFee;
        this.commissionAmount = emergencyFee;   // 팀원 영수증 호환
        this.billedAmount = billedAmount;
        this.vatAmount = vatAmount;
        this.verificationStatus = verificationStatus;
        this.approvalStatus = approvalStatus;
        this.flagReason = flagReason;
    }

    public void approve(Long approverId) {
        this.approvalStatus = ApprovalStatus.APPROVED;
        this.approvedBy = approverId;
    }

    public void reject(Long approverId) {
        this.approvalStatus = ApprovalStatus.REJECTED;
        this.approvedBy = approverId;
    }

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
