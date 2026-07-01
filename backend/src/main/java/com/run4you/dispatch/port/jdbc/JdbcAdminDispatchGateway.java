package com.run4you.dispatch.port.jdbc;

import com.run4you.dispatch.dto.ActiveDispatchResponse;
import com.run4you.dispatch.port.AdminDispatchQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

/**
 * {@link AdminDispatchQueryPort} 의 native SQL 구현.
 *
 * <p>1) 관리자의 역할/브랜드를 확인한 뒤,
 * 2) 진행 중 출동을 브랜드 범위로 조인 조회한다.
 * 현재 상태는 assignments.status, ETA/좌표/시각은 dispatch_status_history 최신 행에서 취득한다.
 */
@Component
@RequiredArgsConstructor
public class JdbcAdminDispatchGateway implements AdminDispatchQueryPort {

    private final JdbcTemplate jdbc;

    private static final String SELECT_ACTIVE = """
            SELECT a.id             AS assignment_id,
                   a.as_request_id  AS as_request_id,
                   a.status         AS status,
                   s.name           AS store_name,
                   eng.name         AS engineer_name,
                   eng.phone        AS engineer_phone,
                   eq.name          AS equipment_name,
                   eq.category      AS equipment_category,
                   r.priority       AS priority,
                   h.eta_minutes    AS eta_minutes,
                   h.latitude       AS latitude,
                   h.longitude      AS longitude,
                   h.changed_at     AS changed_at
            FROM assignments a
            JOIN as_requests r ON r.id = a.as_request_id
            JOIN stores s      ON s.id = r.store_id
            JOIN equipment eq  ON eq.id = r.equipment_id
            JOIN users eng     ON eng.id = a.engineer_id
            LEFT JOIN dispatch_status_history h
                   ON h.id = (
                        SELECT h2.id FROM dispatch_status_history h2
                         WHERE h2.assignment_id = a.id
                         ORDER BY h2.changed_at DESC, h2.id DESC
                         LIMIT 1
                   )
            WHERE a.status IN ('ACCEPTED', 'DISPATCHED', 'ARRIVED', 'REPAIRING')
              AND ( ? = 1 OR s.brand_id = ? )
            ORDER BY (r.priority = 'EMERGENCY') DESC, h.changed_at DESC
            """;

    @Override
    public List<ActiveDispatchResponse> findActiveForAdmin(Long adminUserId) {
        // 1) 관리자 역할/브랜드 확인
        Map<String, Object> admin = jdbc.queryForMap(
                "SELECT role, brand_id FROM users WHERE id = ?", adminUserId);
        boolean isSuper = "SUPER_ADMIN".equals(admin.get("role"));
        Long brandId = admin.get("brand_id") == null ? -1L : ((Number) admin.get("brand_id")).longValue();

        // 2) 활성 출동 조회 (총괄이면 전체, 아니면 본인 브랜드)
        return jdbc.query(SELECT_ACTIVE,
                (rs, i) -> {
                    Timestamp ts = rs.getTimestamp("changed_at");
                    Object eta = rs.getObject("eta_minutes");
                    return new ActiveDispatchResponse(
                            rs.getLong("assignment_id"),
                            rs.getLong("as_request_id"),
                            rs.getString("status"),
                            rs.getString("store_name"),
                            rs.getString("engineer_name"),
                            rs.getString("engineer_phone"),
                            rs.getString("equipment_name"),
                            rs.getString("equipment_category"),
                            rs.getString("priority"),
                            eta == null ? null : ((Number) eta).intValue(),
                            (BigDecimal) rs.getObject("latitude"),
                            (BigDecimal) rs.getObject("longitude"),
                            ts == null ? null : ts.toLocalDateTime()
                    );
                },
                isSuper ? 1 : 0,
                brandId);
    }
}
