package com.run4you.matching.entity;

import com.run4you.asrequest.entity.AsRequest;
import com.run4you.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
  배정 후보 스코어 로그 — 가중치 스코어링 근거 추적 및 튜닝용
  종합 점수 = 거리×0.30 + 전문분야×0.25 + 평점×0.20 + 가용성×0.15 + 긴급도×0.10
 */
@Entity
@Table(name = "assignment_candidates")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class AssignmentCandidate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "as_request_id", nullable = false)
    private AsRequest asRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "engineer_id", nullable = false)
    private User engineer;

    /** 거리 점수 (0~100, 가중치 30%) — 가까울수록 고점 */
    @Column(precision = 5, scale = 2)
    private BigDecimal distanceScore;

    /** 전문분야 일치 점수 (0~100, 가중치 25%) */
    @Column(precision = 5, scale = 2)
    private BigDecimal specialtyScore;

    /** 누적 평점 환산 점수 (0~100, 가중치 20%) */
    @Column(precision = 5, scale = 2)
    private BigDecimal ratingScore;

    /** 가용성 점수 (0~100, 가중치 15%) */
    @Column(precision = 5, scale = 2)
    private BigDecimal availabilityScore;

    /** 긴급도 가중 점수 (0~100, 가중치 10%) */
    @Column(precision = 5, scale = 2)
    private BigDecimal urgencyScore;

    /** 항목별 가중합 종합 점수 */
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal totalScore;

    /** 최종 선택 여부 (수락한 엔지니어만 true) */
    @Column(nullable = false)
    @Builder.Default
    private boolean selected = false;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    public void markSelected() {
        this.selected = true;
    }
}
