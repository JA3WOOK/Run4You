package com.run4you.dispatch.port;

import com.run4you.dispatch.domain.DispatchStatus;

import java.time.LocalDateTime;

/**
 * assignments / as_requests / stores 는 ②·③ 도메인 소유 테이블이다.
 * 본 도메인(④)은 이 포트를 통해서만 읽고, 상태 전이 결과를 반영(쓰기)한다.
 *
 * <p>기본 구현은 {@code port.jdbc.JdbcDispatchGateway} 가 JdbcTemplate 으로 제공한다.
 * 추후 ③ 도메인이 서비스/엔티티를 공개하면 어댑터만 교체하면 된다.
 */
public interface AssignmentGateway {

    /** 배정 + 연관 접수/매장/브랜드 식별자 묶음 조회 */
    AssignmentView getAssignment(Long assignmentId);

    /**
     * 출동 상태 전이를 영속 반영한다.
     * <ul>
     *   <li>assignments.status = newStatus (+ accepted_at / completed_at 갱신)</li>
     *   <li>as_requests.status 를 거시 흐름으로 동기화 (ASSIGNED / IN_PROGRESS / COMPLETED / CANCELLED)</li>
     * </ul>
     */
    void applyDispatchStatus(Long assignmentId, DispatchStatus newStatus, LocalDateTime at);

    /**
     * 배정 컨텍스트 스냅샷.
     *
     * @param assignmentId 배정 ID
     * @param asRequestId  접수 ID
     * @param engineerId   배정 엔지니어 user_id (상태 변경 권한 검증용)
     * @param requesterId  접수자(점주) user_id (SSE 수신 대상)
     * @param storeId      매장 ID (ETA 거리 계산용)
     * @param brandId      브랜드 ID (관제 스트림 수신 대상 해석용)
     * @param currentStatus 현재 출동 상태 (전이 검증 기준)
     */
    record AssignmentView(
            Long assignmentId,
            Long asRequestId,
            Long engineerId,
            Long requesterId,
            Long storeId,
            Long brandId,
            DispatchStatus currentStatus
    ) {}
}
