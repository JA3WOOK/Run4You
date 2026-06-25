package com.run4you.dispatch.port.jdbc;

import com.run4you.dispatch.domain.DispatchStatus;
import com.run4you.dispatch.port.AssignmentGateway;
import com.run4you.dispatch.port.ControlCenterGateway;
import com.run4you.dispatch.port.LocationGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 타 도메인(②·③) 테이블에 대한 읽기/상태 쓰기를 native SQL 로 처리하는 기본 어댑터.
 *
 * <p>본 모듈이 팀원 엔티티에 컴파일 의존하지 않고도 실제 스키마 위에서 단독 동작하도록 한다.
 * ③ 매칭 도메인이 서비스 API 를 공개하면 이 어댑터만 교체(또는 @Primary 빈으로 대체)하면 된다.
 */
@Component
@RequiredArgsConstructor
public class JdbcDispatchGateway implements AssignmentGateway, LocationGateway, ControlCenterGateway {

    private final JdbcTemplate jdbc;

    // ── AssignmentGateway ──────────────────────────────────────────────

    private static final String SELECT_ASSIGNMENT = """
            SELECT a.id            AS assignment_id,
                   a.as_request_id AS as_request_id,
                   a.engineer_id   AS engineer_id,
                   a.status        AS status,
                   r.requester_id  AS requester_id,
                   r.store_id      AS store_id,
                   s.brand_id      AS brand_id
            FROM assignments a
            JOIN as_requests r ON r.id = a.as_request_id
            JOIN stores s      ON s.id = r.store_id
            WHERE a.id = ?
            """;

    @Override
    public AssignmentView getAssignment(Long assignmentId) {
        List<AssignmentView> rows = jdbc.query(SELECT_ASSIGNMENT,
                (rs, i) -> new AssignmentView(
                        rs.getLong("assignment_id"),
                        rs.getLong("as_request_id"),
                        rs.getLong("engineer_id"),
                        rs.getLong("requester_id"),
                        rs.getLong("store_id"),
                        rs.getLong("brand_id"),
                        DispatchStatus.valueOf(rs.getString("status"))
                ),
                assignmentId);
        return rows.isEmpty() ? null : rows.get(0);
    }

    @Override
    public void applyDispatchStatus(Long assignmentId, DispatchStatus newStatus, LocalDateTime at) {
        // 1) assignments 갱신 (수락/완료 시각 동기화)
        jdbc.update("""
                UPDATE assignments
                   SET status       = ?,
                       accepted_at  = CASE WHEN ? = 'ACCEPTED'  THEN ? ELSE accepted_at  END,
                       completed_at = CASE WHEN ? = 'COMPLETED' THEN ? ELSE completed_at END,
                       updated_at   = ?
                 WHERE id = ?
                """,
                newStatus.name(),
                newStatus.name(), at,
                newStatus.name(), at,
                at, assignmentId);

        // 2) as_requests 거시 흐름 동기화
        String asStatus = toAsRequestStatus(newStatus);
        if (asStatus != null) {
            jdbc.update("""
                    UPDATE as_requests r
                    JOIN assignments a ON a.as_request_id = r.id
                       SET r.status = ?, r.updated_at = ?
                     WHERE a.id = ?
                    """, asStatus, at, assignmentId);
        }
    }

    /** 출동(미시) 상태 → A/S 접수(거시) 상태 매핑 */
    private String toAsRequestStatus(DispatchStatus s) {
        return switch (s) {
            case ACCEPTED -> "ASSIGNED";
            case DISPATCHED, ARRIVED, REPAIRING -> "IN_PROGRESS";
            case COMPLETED -> "COMPLETED";
            case CANCELLED -> "CANCELLED";
            case PENDING_ACCEPT -> null; // 거시 상태 변경 없음
        };
    }

    // ── LocationGateway ────────────────────────────────────────────────

    @Override
    public Optional<GeoPoint> storeLocation(Long storeId) {
        return jdbc.query(
                "SELECT latitude, longitude FROM stores WHERE id = ?",
                (rs, i) -> new GeoPoint(rs.getBigDecimal("latitude"), rs.getBigDecimal("longitude")),
                storeId
        ).stream().findFirst();
    }

    @Override
    public Optional<GeoPoint> engineerLocation(Long engineerUserId) {
        return jdbc.query(
                "SELECT current_latitude, current_longitude FROM engineer_profiles WHERE user_id = ?",
                (rs, i) -> new GeoPoint(rs.getBigDecimal("current_latitude"), rs.getBigDecimal("current_longitude")),
                engineerUserId
        ).stream().findFirst();
    }

    // ── ControlCenterGateway ───────────────────────────────────────────

    @Override
    public List<Long> controlCenterUserIds(Long brandId) {
        return jdbc.queryForList("""
                SELECT id FROM users
                 WHERE deleted_at IS NULL
                   AND status = 'APPROVED'
                   AND ( role = 'SUPER_ADMIN'
                      OR (role = 'BRAND_ADMIN' AND brand_id = ?) )
                """, Long.class, brandId);
    }
}
