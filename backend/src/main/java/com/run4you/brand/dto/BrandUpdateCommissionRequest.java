package com.run4you.brand.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import java.math.BigDecimal;

@Getter
public class BrandUpdateCommissionRequest {

    @NotNull(message = "수수료율을 입력해주세요.")
    @Positive(message = "수수료율은 0보다 커야 합니다.")
    private BigDecimal commissionRate;
}
