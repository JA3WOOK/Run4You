package com.run4you.settlement.entity;

/** 정산 승인 상태 */
public enum ApprovalStatus {
    PENDING,   // 검토 대기
    APPROVED,  // 승인 완료
    REJECTED,  // 반려
    PAID       // 지급 완료
}
