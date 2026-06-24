package com.run4you.brand.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "brands")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class Brand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "business_no", nullable = false, unique = true)
    private String businessNo;

    @Column(name = "commission_rate", nullable = false)
    private Double commissionRate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BrandStatus status;

    public void approve() {
        this.status = BrandStatus.ACTIVE;
    }

    public void reject() {
        this.status = BrandStatus.INACTIVE;
    }

    public void updateCommissionRate(Double commissionRate) {
        this.commissionRate = commissionRate;
    }
}
