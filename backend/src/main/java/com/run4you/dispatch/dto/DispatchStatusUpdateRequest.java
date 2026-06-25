package com.run4you.dispatch.dto;

import com.run4you.dispatch.domain.DispatchStatus;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * 엔지니어의 출동 상태 변경 요청.
 * 위경도는 이동 단계(DISPATCHED/ARRIVED)에서 현재 위치 전송용 — ETA 산출/이력 적재에 사용.
 */
public record DispatchStatusUpdateRequest(

        @NotNull(message = "변경할 상태(status)는 필수입니다.")
        DispatchStatus status,

        @DecimalMin(value = "-90.0") @DecimalMax(value = "90.0")
        BigDecimal latitude,

        @DecimalMin(value = "-180.0") @DecimalMax(value = "180.0")
        BigDecimal longitude
) {}
