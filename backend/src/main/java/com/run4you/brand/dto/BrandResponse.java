package com.run4you.brand.dto;

import com.run4you.brand.entity.Brand;
import com.run4you.brand.entity.BrandStatus;
import lombok.Getter;
import java.math.BigDecimal;

@Getter
public class BrandResponse {

    private final Long id;
    private final String name;
    private final String businessNo;
    private final BigDecimal commissionRate;
    private final BrandStatus status;

    public BrandResponse(Brand brand) {
        this.id = brand.getId();
        this.name = brand.getName();
        this.businessNo = brand.getBusinessNo();
        this.commissionRate = brand.getCommissionRate();
        this.status = brand.getStatus();
    }
}
