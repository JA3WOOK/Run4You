package com.run4you.report.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 정비 리포트 (테이블 repair_reports, 배정과 1:1).
 * 수리 완료 후 엔지니어가 작성하며 공임비·부품비·진단 의견을 기록한다.
 *
 * <p>[디커플링] 앞 단계(배정/접수/엔지니어/기자재)는 ID(Long) 값으로만 참조.
 */
@Entity
@Table(name = "repair_reports")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RepairReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "assignment_id", nullable = false, unique = true)
    private Long assignmentId;

    @Column(name = "as_request_id", nullable = false)
    private Long asRequestId;

    @Column(name = "engineer_id", nullable = false)
    private Long engineerId;

    @Column(name = "equipment_id")
    private Long equipmentId;

    @Column(name = "labor_cost", nullable = false, precision = 12, scale = 2)
    private BigDecimal laborCost;

    @Column(name = "parts_cost", nullable = false, precision = 12, scale = 2)
    private BigDecimal partsCost;

    @Column(name = "total_cost", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalCost;

    @Column(columnDefinition = "TEXT")
    private String diagnosis;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "report", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RepairReportParts> parts = new ArrayList<>();

    @Builder
    private RepairReport(Long assignmentId, Long asRequestId, Long engineerId, Long equipmentId,
                         BigDecimal laborCost, String diagnosis) {
        this.assignmentId = assignmentId;
        this.asRequestId = asRequestId;
        this.engineerId = engineerId;
        this.equipmentId = equipmentId;
        this.laborCost = laborCost;
        this.diagnosis = diagnosis;
        this.partsCost = BigDecimal.ZERO;
        this.totalCost = BigDecimal.ZERO;
    }

    /** 부품 사용 내역 추가 (양방향 연관 동기화) */
    public void addPart(RepairReportParts part) {
        part.setReport(this);
        this.parts.add(part);
    }

    /** 부품비·총비용 재계산. partsCost = Σ(청구단가×수량), totalCost = partsCost + laborCost */
    public void recalculateCosts() {
        BigDecimal sum = BigDecimal.ZERO;
        for (RepairReportParts p : parts) {
            sum = sum.add(p.getLineTotal());
        }
        this.partsCost = sum;
        this.totalCost = sum.add(laborCost == null ? BigDecimal.ZERO : laborCost);
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
