package com.run4you.lms.repository;

import com.run4you.lms.dto.EngineerTrainingRow;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.List;

/**
 * 교육 이수 현황 집계 전용 조회 리포지토리 (읽기 전용).
 *
 * <p>enrollments · exam_attempts 를 users 에 직접 조인하면 fan-out 으로 카운트가
 * 부풀려지므로, 각 테이블을 <b>엔지니어 단위로 먼저 GROUP BY 한 파생 테이블</b>로
 * 만든 뒤 users 에 1:1 로 LEFT JOIN 한다. 이러면 수강 수·응시 수가 서로 곱해지지 않는다.</p>
 *
 * <p>엔지니어 스택(com.run4you.education / com.run4you.lms) 의 엔티티·리포지토리를
 * 전혀 수정하지 않도록, 팀원 코드에 의존하지 않는 native 집계 쿼리로 분리했다.</p>
 */
@Repository
@RequiredArgsConstructor
public class TrainingStatsRepository {

    private final JdbcTemplate jdbcTemplate;

    private static final String SQL = """
            SELECT
                u.id                            AS engineer_id,
                u.name                          AS engineer_name,
                ep.skill_grade                  AS skill_grade,
                COALESCE(en.enrolled_count, 0)  AS enrolled_count,
                COALESCE(en.completed_count, 0) AS completed_count,
                en.avg_progress                 AS avg_progress,
                COALESCE(ea.attempted_count, 0) AS attempted_count,
                COALESCE(ea.passed_count, 0)    AS passed_count,
                ea.last_attempted_at            AS last_attempted_at
            FROM users u
            LEFT JOIN engineer_profiles ep ON ep.user_id = u.id
            LEFT JOIN (
                SELECT engineer_id,
                       COUNT(*)                                              AS enrolled_count,
                       SUM(CASE WHEN status = 'COMPLETED' THEN 1 ELSE 0 END) AS completed_count,
                       AVG(progress_rate)                                    AS avg_progress
                FROM enrollments
                GROUP BY engineer_id
            ) en ON en.engineer_id = u.id
            LEFT JOIN (
                SELECT engineer_id,
                       COUNT(DISTINCT exam_id)                                AS attempted_count,
                       COUNT(DISTINCT CASE WHEN passed = 1 THEN exam_id END)  AS passed_count,
                       MAX(attempted_at)                                      AS last_attempted_at
                FROM exam_attempts
                GROUP BY engineer_id
            ) ea ON ea.engineer_id = u.id
            WHERE u.role = 'ENGINEER'
            ORDER BY completed_count DESC, u.name ASC
            """;

    public List<EngineerTrainingRow> findEngineerTrainingRows() {
        return jdbcTemplate.query(SQL, (rs, rowNum) -> {
            long enrolled = rs.getLong("enrolled_count");
            long completed = rs.getLong("completed_count");
            double completionRate = enrolled == 0
                    ? 0.0
                    : Math.round(completed * 1000.0 / enrolled) / 10.0;

            BigDecimal avg = rs.getBigDecimal("avg_progress");
            double avgProgress = avg == null
                    ? 0.0
                    : avg.setScale(2, RoundingMode.HALF_UP).doubleValue();

            Timestamp ts = rs.getTimestamp("last_attempted_at");

            return new EngineerTrainingRow(
                    rs.getLong("engineer_id"),
                    rs.getString("engineer_name"),
                    rs.getString("skill_grade"),
                    enrolled,
                    completed,
                    completionRate,
                    rs.getLong("attempted_count"),
                    rs.getLong("passed_count"),
                    avgProgress,
                    ts == null ? null : ts.toLocalDateTime()
            );
        });
    }
}
