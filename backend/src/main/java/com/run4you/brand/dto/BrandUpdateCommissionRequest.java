package com.run4you.brand.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import java.math.BigDecimal;

@Getter
public class BrandUpdateCommissionRequest {

    @NotNull(message = "수수료율을 입력해주세요.")
    @DecimalMin(value = "0.0", inclusive = false, message = "수수료율은 0보다 커야 합니다.")
    @DecimalMax(value = "100.0", message = "수수료율은 100을 초과할 수 없습니다.")
    private BigDecimal commissionRate;
}
