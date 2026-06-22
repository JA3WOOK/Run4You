package com.run4you.equipment.entity;

import com.run4you.store.entity.Store;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(name = "equipment")
public class Equipment { // 점포별 도입 기자재 자산 테이블

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 소속 매장
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    // 기기 별칭 (예: 키오스크 2호기)
    @Column(length = 100)
    private String name;

    // 시리얼 번호 (유니크)
    @Column(nullable = false, unique = true, length = 100)
    private String serialNo;

    // 제조사
    @Column(length = 100)
    private String manufacturer;

    // 모델명
    @Column(length = 100)
    private String modelName;

    // 기자재 카테고리
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EquipmentCategory category;

    // 기자재 상태 (기본값: 정상)
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EquipmentStatus status = EquipmentStatus.OPERATIONAL;

    // 구매일
    @Column
    private LocalDate purchasedAt;

    // 다음 점검 예정일
    @Column
    private LocalDate nextInspectionDate;

    // 소프트 삭제 (null = 활성)
    @Column
    private LocalDateTime deletedAt;

    // 생성 일시 (자동)
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 수정 일시 (자동)
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // 기자재 상태 변경 (A/S 접수 시 호출)
    public void updateStatus(EquipmentStatus status) {
        this.status = status;
    }
}