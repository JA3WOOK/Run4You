package com.run4you.matching.repository;

import com.run4you.common.enums.DispatchStatus;
import com.run4you.matching.entity.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
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

    /** 기자재별 가장 최근 수리 완료 시각 조회 (수리 이력 모달 최근 수리일 표시용) */
    @Query("""
            SELECT a.completedAt FROM Assignment a
            WHERE a.asRequest.equipment.id = :equipmentId
              AND a.completedAt IS NOT NULL
            ORDER BY a.completedAt DESC
            LIMIT 1
            """)
    Optional<LocalDateTime> findLastRepairAtByEquipmentId(@Param("equipmentId") Long equipmentId);

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

    /** as_request_id 목록으로 완료된 배정 한번에 조회 */
    @Query("""
            SELECT a FROM Assignment a
            WHERE a.asRequest.id IN :asRequestIds
              AND a.completedAt IS NOT NULL
            """)
    List<Assignment> findCompletedByAsRequestIds(@Param("asRequestIds") List<Long> asRequestIds);

    /** 엔지니어의 배정 이력 조회 */
    List<Assignment> findByEngineerIdOrderByAssignedAtDesc(Long engineerId);

    /** as_request_id로 배정 한 건 조회 (엔지니어 포함) */
    @Query("""
            SELECT a FROM Assignment a
            LEFT JOIN FETCH a.engineer
            WHERE a.asRequest.id = :asRequestId
            """)
    Optional<Assignment> findByAsRequestIdWithEngineer(@Param("asRequestId") Long asRequestId);

    /** 브랜드별 평균 처리시간 산출용 — 완료된 배정의 접수시각·완료시각 (SUPER_ADMIN 대시보드) */
    @Query("""
            SELECT b.id, ar.requestedAt, a.completedAt
            FROM Assignment a
            JOIN a.asRequest ar
            JOIN ar.store s
            JOIN s.brand b
            WHERE a.completedAt IS NOT NULL
            """)
    List<Object[]> findCompletedTimesWithBrand();
}