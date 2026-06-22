package com.run4you.matching.repository;

import com.run4you.common.enums.DispatchStatus;
import com.run4you.matching.entity.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

    /** 특정 AS 요청의 활성 배정 존재 여부 확인 (중복 배정 방지용) */
    boolean existsByAsRequestIdAndStatusNotIn(Long asRequestId, List<DispatchStatus> excludedStatuses);

    /** 엔지니어의 현재 진행 중인 배정 건수 (가용성 스코어 산출용) */
    @Query("""
            SELECT COUNT(a) FROM Assignment a
            WHERE a.engineer.id = :engineerId
              AND a.status NOT IN ('COMPLETED', 'CANCELLED')
            """)
    int countActiveByEngineerId(@Param("engineerId") Long engineerId);

    /** 특정 AS 요청의 최종 배정 조회 */
    @Query("""
            SELECT a FROM Assignment a
            JOIN FETCH a.engineer
            JOIN FETCH a.asRequest ar
            WHERE ar.id = :asRequestId
              AND a.status NOT IN ('CANCELLED')
            ORDER BY a.assignedAt DESC
            """)
    Optional<Assignment> findActiveByAsRequestId(@Param("asRequestId") Long asRequestId);

    /** 엔지니어의 배정 이력 조회 */
    List<Assignment> findByEngineerIdOrderByAssignedAtDesc(Long engineerId);
}
