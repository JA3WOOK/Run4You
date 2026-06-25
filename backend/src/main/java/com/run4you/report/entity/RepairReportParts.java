package com.run4you.report.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 정비 리포트 부품 사용 내역 (테이블 repair_report_parts).
 * 청구 단가(appliedPrice)를 부품 마스터 표준 단가(standardPrice)와 대조하여
 * priceMatched 로 위변조 여부를 기록한다.
 */
@Entity
@Table(name = "repair_report_parts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RepairReportParts {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id", nullable = false)
    private RepairReport report;

    @Column(name = "part_id", nullable = false)
    private Long partId;

    @Column(name = "part_code", length = 50)
    private String partCode;

    @Column(name = "part_name", length = 100)
    private String partName;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "applied_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal appliedPrice;

    @Column(name = "standard_price", precision = 12, scale = 2)
    private BigDecimal standardPrice;

    @Column(name = "price_matched")
    private boolean priceMatched;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Builder
    private RepairReportParts(Long partId, String partCode, String partName, int quantity,
                             BigDecimal appliedPrice, BigDecimal standardPrice, boolean priceMatched) {
        this.partId = partId;
        this.partCode = partCode;
        this.partName = partName;
        this.quantity = quantity;
        this.appliedPrice = appliedPrice;
        this.standardPrice = standardPrice;
        this.priceMatched = priceMatched;
    }

    public BigDecimal getLineTotal() {
        return appliedPrice.multiply(BigDecimal.valueOf(quantity));
    }

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
