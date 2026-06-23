package com.run4you.settlement.dto;

import jakarta.validation.constraints.NotNull;

/**
 * 정비 리포트로부터 정산 생성 요청.
 * priority 는 앞 단계(A/S 접수)의 우선순위(EMERGENCY/NORMAL) — 긴급수수료 계산에 사용.
 */
public record SettlementGenerateRequest(

        @NotNull(message = "리포트 ID는 필수입니다.")
        Long reportId,

        // "EMERGENCY" 또는 "NORMAL" (null 이면 NORMAL 로 처리)
        String priority,

        // 청구 대상 본사 ID (없으면 null)
        Long brandId
) {
}
