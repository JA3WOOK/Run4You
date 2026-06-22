package com.run4you.store.entity;

import com.run4you.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "stores")
public class Store {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 점주 (users 테이블 참조)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    // 매장명
    @Column(nullable = false, length = 100)
    private String name;

    // 주소
    @Column(length = 255)
    private String address;

    // 위도 (배정 거리 계산용)
    @Column(precision = 10, scale = 7)
    private BigDecimal latitude;

    // 경도
    @Column(precision = 10, scale = 7)
    private BigDecimal longitude;

    // 매장 연락처
    @Column(length = 20)
    private String phone;

    // 생성 일시 (자동)
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 수정 일시 (자동)
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}