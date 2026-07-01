package com.run4you.brand.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

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

    @Column(name = "commission_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal commissionRate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BrandStatus status;

    public void approve() {
        this.status = BrandStatus.ACTIVE;
    }

    public void updateCommissionRate(BigDecimal commissionRate) {
        this.commissionRate = commissionRate;
    }
}
