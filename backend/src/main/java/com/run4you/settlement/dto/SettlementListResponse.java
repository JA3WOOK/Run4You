package com.run4you.settlement.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * 정산 관리 화면 응답 = 상단 요약 카드 + 정산 목록.
 * (피그마: 검토 대기 금액 / 승인 완료 금액 / 위변조 의심 건수 카드)
 */
public record SettlementListResponse(
        Summary summary,
        List<SettlementResponse> items
) {
    public record Summary(
            BigDecimal reviewPendingAmount,  // 검토 대기(승인 PENDING) 청구금액 합계
            BigDecimal approvedAmount,       // 승인 완료(APPROVED) 청구금액 합계
            long flaggedCount                // 위변조 의심(FLAGGED) 건수
    ) {
    }
}
