package com.run4you.matching.entity;

import com.run4you.common.enums.AvailabilityStatus;
import com.run4you.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 엔지니어 부가 정보 — 배정 스코어링(거리·평점·가용성) 핵심 입력 데이터
 * users 테이블과 1:1 연관
 */
@Entity
@Table(name = "engineer_profiles",
        uniqueConstraints = @UniqueConstraint(name = "uq_eng_profile_user", columnNames = "user_id"))
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class EngineerProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** FK → users (ENGINEER 권한) */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** 누적 평점 (0.00 ~ 5.00) — 스코어링 평점 항목 입력값 */
    @Column(nullable = false, precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal rating = BigDecimal.ZERO;

    /** 평가 수 — 평점 신뢰도 보정용 */
    @Column(nullable = false)
    @Builder.Default
    private Integer ratingCount = 0;

    /** 재고장률 (%) */
    @Column(nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal revisitRate = BigDecimal.ZERO;

    /** 출동 가능 반경 (km) — 반경 초과 요청은 대기열 제외 */
    @Column(nullable = false)
    @Builder.Default
    private Integer serviceRadiusKm = 5;

    /** 가용 상태 — 배정 스코어링 가용성 항목 */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private AvailabilityStatus availabilityStatus = AvailabilityStatus.OFFLINE;

    /** 당일 처리 한도 */
    @Column(nullable = false)
    @Builder.Default
    private Integer dailyCapacity = 5;

    /** 기술 등급 (LMS 필기시험 합격 시 상향 — 확장) */
    @Column(nullable = false, length = 20)
    @Builder.Default
    private String skillGrade = "BEGINNER";

    /** 실시간 위도 — 배정 스코어링 거리 항목 입력값 */
    @Column(precision = 10, scale = 7)
    private BigDecimal currentLatitude;

    /** 실시간 경도 */
    @Column(precision = 10, scale = 7)
    private BigDecimal currentLongitude;

    /** 위치 갱신 시각 */
    private LocalDateTime locationUpdatedAt;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /** 전문 분야 목록 (양방향 편의 관계) */
    @OneToMany(mappedBy = "engineerProfile", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<EngineerSpecialty> specialties = new ArrayList<>();

    // ─── 비즈니스 메서드 ────────────────────────────────────────────

    /** 위치 갱신 */
    public void updateLocation(BigDecimal latitude, BigDecimal longitude) {
        this.currentLatitude = latitude;
        this.currentLongitude = longitude;
        this.locationUpdatedAt = LocalDateTime.now();
    }

    /** 가용 상태 변경 */
    public void changeAvailability(AvailabilityStatus status) {
        this.availabilityStatus = status;
    }

    /** 평점 업데이트 (이동 평균) */
    public void updateRating(int newRating) {
        BigDecimal totalSum = this.rating.multiply(BigDecimal.valueOf(this.ratingCount))
                .add(BigDecimal.valueOf(newRating));
        this.ratingCount++;
        this.rating = totalSum.divide(BigDecimal.valueOf(this.ratingCount), 2, java.math.RoundingMode.HALF_UP);
    }

    /** 출동 가능 여부 */
    public boolean isDispatchable() {
        return this.availabilityStatus != AvailabilityStatus.OFFLINE
                && this.currentLatitude != null
                && this.currentLongitude != null;
    }
}