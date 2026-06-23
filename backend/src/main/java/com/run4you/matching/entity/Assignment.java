package com.run4you.matching.entity;

import com.run4you.asrequest.entity.AsRequest;
import com.run4you.common.enums.AssignMethod;
import com.run4you.common.enums.DispatchStatus;
import com.run4you.common.exception.InvalidStatusTransitionException;
import com.run4you.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
  엔지니어 배정 — 출동 단위 미시 흐름 상태머신
  as_request당 최종 1건 수락.
  동시성 주의: 수락 처리 시 Redisson 분산 락으로 중복 배정 차단 (lock:as:{asRequestId})
 */
@Entity
@Table(name = "assignments",
        indexes = {
            @Index(name = "idx_assign_asreq", columnList = "as_request_id"),
            @Index(name = "idx_assign_engineer_status", columnList = "engineer_id, status")
        })
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class Assignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** A/S 접수 건 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "as_request_id", nullable = false)
    private AsRequest asRequest;

    /** 배정된 엔지니어 (ENGINEER 권한) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "engineer_id", nullable = false)
    private User engineer;

    /** 가중치 스코어링 종합 점수 (로그 목적) */
    @Column(precision = 5, scale = 2)
    private BigDecimal totalScore;

    /** 배정 방식 */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private AssignMethod assignMethod = AssignMethod.MANUAL_ACCEPT;

    /** 현재 출동 단계 (상태머신) */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private DispatchStatus status = DispatchStatus.PENDING_ACCEPT;

    /** 배정 시각 */
    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime assignedAt = LocalDateTime.now();

    /** 수락 시각 */
    private LocalDateTime acceptedAt;

    /** 완료 시각 */
    private LocalDateTime completedAt;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // ─── 팩토리 메서드 ───────────────────────────────────────────────

    public static Assignment create(AsRequest asRequest, User engineer, BigDecimal totalScore) {
        return Assignment.builder()
                .asRequest(asRequest)
                .engineer(engineer)
                .totalScore(totalScore)
                .assignMethod(AssignMethod.MANUAL_ACCEPT)
                .status(DispatchStatus.ACCEPTED)
                .assignedAt(LocalDateTime.now())
                .acceptedAt(LocalDateTime.now())
                .build();
    }

    // ─── 상태머신 전이 ──────────────────────────────────────────────

    /**
      출동 상태 전이 (DISPATCHED → ARRIVED → REPAIRING → COMPLETED 순서만 허용)
      상태 변경 후 SSE 발행은 서비스 레이어에서 처리
     */
    public void transitionTo(DispatchStatus next) {
        validateTransition(this.status, next);
        this.status = next;
        if (next == DispatchStatus.COMPLETED) {
            this.completedAt = LocalDateTime.now();
        }
    }

    private void validateTransition(DispatchStatus current, DispatchStatus next) {
        boolean valid = switch (current) {
            case ACCEPTED          -> next == DispatchStatus.DISPATCHED || next == DispatchStatus.CANCELLED;
            case DISPATCHED        -> next == DispatchStatus.ARRIVED    || next == DispatchStatus.CANCELLED;
            case ARRIVED           -> next == DispatchStatus.REPAIRING  || next == DispatchStatus.CANCELLED;
            case REPAIRING         -> next == DispatchStatus.COMPLETED;
            case PENDING_ACCEPT, COMPLETED, CANCELLED -> false;
        };
        if (!valid) {
            throw new InvalidStatusTransitionException(
                    String.format("상태 전이 불가: %s → %s", current, next));
        }
    }
}