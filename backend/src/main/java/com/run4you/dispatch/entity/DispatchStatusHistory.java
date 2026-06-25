package com.run4you.dispatch.entity;

import com.run4you.dispatch.domain.DispatchStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 출동 상태 변경 이력. assignments.status 가 바뀔 때마다 1행 적재되며,
 * 적재 시점이 SSE 실시간 알림 발행 트리거가 된다. (ERD §4-11)
 */
@Entity
@Table(name = "dispatch_status_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DispatchStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "assignment_id", nullable = false)
    private Long assignmentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private DispatchStatus status;

    /** 상태 변경 시점 엔지니어 위도 */
    @Column(name = "latitude", precision = 10, scale = 7)
    private BigDecimal latitude;

    /** 상태 변경 시점 엔지니어 경도 */
    @Column(name = "longitude", precision = 10, scale = 7)
    private BigDecimal longitude;

    /** 거리 기반 간이 ETA(분) — 정밀 ETA는 2단계(T Map) */
    @Column(name = "eta_minutes")
    private Integer etaMinutes;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    private DispatchStatusHistory(Long assignmentId, DispatchStatus status,
                                  BigDecimal latitude, BigDecimal longitude,
                                  Integer etaMinutes, LocalDateTime changedAt) {
        this.assignmentId = assignmentId;
        this.status = status;
        this.latitude = latitude;
        this.longitude = longitude;
        this.etaMinutes = etaMinutes;
        this.changedAt = changedAt != null ? changedAt : LocalDateTime.now();
    }
}