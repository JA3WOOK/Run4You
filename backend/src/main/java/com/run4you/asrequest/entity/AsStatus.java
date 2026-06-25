package com.run4you.asrequest.entity;

// 접수 상태
public enum AsStatus {
    RECEIVED,       // 접수 완료
    MATCHING,       // 엔지니어 매칭 중
    ASSIGNED,       // 엔지니어 배정 완료
    IN_PROGRESS,    // 출동/수리 진행 중
    COMPLETED,      // 수리 완료
    CANCELLED       // 접수 취소
}
