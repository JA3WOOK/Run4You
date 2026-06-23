package com.run4you.settlement.entity;

/** 정산 정합성 검증 상태 */
public enum VerificationStatus {
    PENDING,   // 검증 전
    VERIFIED,  // 단가 일치 — 정상
    FLAGGED    // 단가 불일치 — 위변조 의심
}
