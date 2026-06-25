package com.run4you.common.enums;

public enum DispatchStatus {
    PENDING_ACCEPT,  // 배정 완료, 엔지니어 수락 대기
    ACCEPTED,        // 엔지니어 수락 완료
    DISPATCHED,      // 출동 시작
    ARRIVED,         // 현장 도착
    REPAIRING,       // 수리 개시
    COMPLETED,       // 수리 완료
    CANCELLED        // 취소
}
