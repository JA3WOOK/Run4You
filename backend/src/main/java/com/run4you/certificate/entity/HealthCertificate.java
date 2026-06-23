package com.run4you.certificate.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 기기 건강 진단서.
 * 수리 완료 후 발급되며, §20 감점식으로 health_score(0~100) 와 등급(A~D)을 산정한다.
 * 산정에 쓰인 4개 지표 값도 함께 기록해 근거를 남긴다.
 *
 * <p>[디커플링] equipmentId/reportId 는 ID(Long)로만 참조.
 */
@Entity
@Table(name = "health_certificates")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HealthCertificate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "equipment_id", nullable = false)
    private Long equipmentId;

    @Column(name = "report_id")
    private Long reportId;

    @Column(name = "certificate_no", nullable = false, unique = true, length = 50)
    private String certificateNo;

    @Column(name = "health_score", nullable = false)
    private int healthScore;

    @Column(nullable = false, length = 1)
    @Enumerated(EnumType.STRING)
    private HealthGrade grade;

    // 산정 근거 (발급 시점 스냅샷)
    @Column(name = "repair_count")
    private int repairCount;

    @Column(name = "usage_years")
    private int usageYears;

    @Column(name = "replaced_parts_count")
    private int replacedPartsCount;

    @Column(name = "recent_fault")
    private boolean recentFault;

    @Column(name = "issued_at")
    private LocalDateTime issuedAt;

    @Column(name = "file_path", length = 255)
    private String filePath;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Builder
    private HealthCertificate(Long equipmentId, Long reportId, String certificateNo,
                              int healthScore, HealthGrade grade, int repairCount, int usageYears,
                              int replacedPartsCount, boolean recentFault) {
        this.equipmentId = equipmentId;
        this.reportId = reportId;
        this.certificateNo = certificateNo;
        this.healthScore = healthScore;
        this.grade = grade;
        this.repairCount = repairCount;
        this.usageYears = usageYears;
        this.replacedPartsCount = replacedPartsCount;
        this.recentFault = recentFault;
    }

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.issuedAt = now;
        this.createdAt = now;
    }
}
