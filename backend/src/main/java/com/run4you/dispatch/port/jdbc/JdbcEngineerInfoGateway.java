package com.run4you.dispatch.port.jdbc;

import com.run4you.dispatch.dto.AssignmentEngineerResponse;
import com.run4you.dispatch.port.EngineerInfoPort;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * {@link EngineerInfoPort} 의 native SQL 구현.
 * assignments → users(엔지니어) → engineer_profiles(1:1) 를 조인해 표시용 정보를 조립한다.
 */
@Component
@RequiredArgsConstructor
public class JdbcEngineerInfoGateway implements EngineerInfoPort {

    private final JdbcTemplate jdbc;

    private static final String SELECT_ENGINEER = """
            SELECT a.engineer_id  AS engineer_id,
                   u.name         AS name,
                   u.phone        AS phone,
                   ep.rating      AS rating,
                   ep.rating_count AS rating_count,
                   ep.skill_grade AS skill_grade
            FROM assignments a
            JOIN users u ON u.id = a.engineer_id
            LEFT JOIN engineer_profiles ep ON ep.user_id = a.engineer_id
            WHERE a.id = ?
            """;

    @Override
    public AssignmentEngineerResponse findByAssignment(Long assignmentId) {
        List<AssignmentEngineerResponse> rows = jdbc.query(SELECT_ENGINEER,
                (rs, i) -> {
                    Object cnt = rs.getObject("rating_count");
                    return new AssignmentEngineerResponse(
                            rs.getLong("engineer_id"),
                            rs.getString("name"),
                            rs.getString("phone"),
                            (BigDecimal) rs.getObject("rating"),
                            cnt == null ? null : ((Number) cnt).intValue(),
                            rs.getString("skill_grade")
                    );
                },
                assignmentId);
        return rows.isEmpty() ? null : rows.get(0);
    }
}
