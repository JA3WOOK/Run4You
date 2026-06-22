package com.run4you.asrequest.entity;

import com.run4you.equipment.entity.Equipment;
import com.run4you.store.entity.Store;
import com.run4you.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "as_requests")
public class AsRequest { // 긴급 A/S 접수 마스터 테이블

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 대상 기자재
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_id", nullable = false)
    private Equipment equipment;

    // 매장 (조회 최적화용)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    // 접수자 (점주)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;

    // 고장 증상 (자연어 입력)
    @Column(columnDefinition = "TEXT")
    private String symptom;

    // 표준 에러코드 (2단계 AI 정형화)
    @Column(length = 50)
    private String errorCode;

    // 고장 카테고리
    @Column(length = 30)
    private String faultCategory;

    // 우선순위 (기본값: 일반)
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Priority priority = Priority.NORMAL;

    // 접수 상태 (기본값: 접수완료)
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AsStatus status = AsStatus.RECEIVED;

    // 접수 시각
    @Column(nullable = false)
    private LocalDateTime requestedAt;

    // 생성 일시 (자동)
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 수정 일시 (자동)
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}