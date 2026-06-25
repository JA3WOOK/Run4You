package com.run4you.report.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

/**
 * 정비 리포트 작성 시 교체 부품 한 줄.
 * 화면 입력: 부품코드 / 수량 / 청구단가.
 */
public record PartLineRequest(

        @NotBlank(message = "부품 코드는 필수입니다.")
        String partCode,

        @Min(value = 1, message = "수량은 1 이상이어야 합니다.")
        int quantity,

        @NotNull(message = "청구 단가는 필수입니다.")
        @PositiveOrZero(message = "청구 단가는 0 이상이어야 합니다.")
        BigDecimal appliedPrice
) {
}
