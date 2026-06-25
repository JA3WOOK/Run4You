package com.run4you.dispatch.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * 출동 중 엔지니어 현재 좌표 ping. 상태 변경 없이 지도/ETA 만 실시간 갱신할 때 사용.
 */
public record EngineerLocationPing(

        @NotNull @DecimalMin("-90.0") @DecimalMax("90.0")
        BigDecimal latitude,

        @NotNull @DecimalMin("-180.0") @DecimalMax("180.0")
        BigDecimal longitude
) {}
