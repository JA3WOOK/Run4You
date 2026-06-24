package com.run4you.certificate.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * 진단서 발급 요청.
 *  - repairCount(수리 횟수)·replacedPartsCount(교체 부품 수)는 내 리포트 테이블에서 자동 집계.
 *  - purchasedAt(구매일)·recentFault(최근 90일 내 고장)는 기자재/접수 도메인(팀원) 값이라 입력받는다.
 */
public record CertificateIssueRequest(

        @NotNull(message = "기자재 ID는 필수입니다.")
        Long equipmentId,

        // 발급 근거 리포트 ID (선택)
        Long reportId,

        @NotNull(message = "구매일은 필수입니다.")
        LocalDate purchasedAt,

        // 최근 90일 내 고장 여부
        boolean recentFault
) {
}
