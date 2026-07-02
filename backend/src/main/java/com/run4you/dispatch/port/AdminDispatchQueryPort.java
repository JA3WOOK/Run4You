package com.run4you.dispatch.port;

import com.run4you.dispatch.dto.ActiveDispatchResponse;

import java.util.List;

/**
 * 통합 관제용 활성 출동 목록 조회 포트.
 *
 * <p>타 도메인(②·③) 테이블을 native SQL 로 조인해 브랜드 범위로 집계한다.
 * 기존 {@code JdbcDispatchGateway} 를 건드리지 않도록 별도 어댑터로 구현한다.
 */
public interface AdminDispatchQueryPort {

    /**
     * 관리자 계정 기준 진행 중 출동 목록.
     * <ul>
     *   <li>BRAND_ADMIN — 본인 브랜드 소속 점포의 출동만</li>
     *   <li>SUPER_ADMIN — 전체 브랜드</li>
     * </ul>
     * 활성 = assignments.status ∈ (ACCEPTED, DISPATCHED, ARRIVED, REPAIRING)
     */
    List<ActiveDispatchResponse> findActiveForAdmin(Long adminUserId);
}
