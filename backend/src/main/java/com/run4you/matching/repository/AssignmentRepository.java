package com.run4you.matching.repository;

import com.run4you.matching.entity.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

    // 기자재별 가장 최근 수리 완료 시각 조회 (수리 이력 보기 모달 상단 최근 수리일 표시용)
    @Query("""
        SELECT a.completedAt FROM Assignment a
        JOIN AsRequest r ON r.id = a.asRequestId
        WHERE r.equipment.id = :equipmentId
        AND a.completedAt IS NOT NULL
        ORDER BY a.completedAt DESC
        LIMIT 1
        """)
    Optional<LocalDateTime> findLastRepairAtByEquipmentId(
            @Param("equipmentId") Long equipmentId);

    // as_request_id 목록으로 완료 시각 한번에 조회
    @Query("""
        SELECT a FROM Assignment a
        WHERE a.asRequestId IN :asRequestIds
        AND a.completedAt IS NOT NULL
        """)
    List<Assignment> findCompletedByAsRequestIds(@Param("asRequestIds") List<Long> asRequestIds);

    // 상세 - as_request_id로 배정 한 건 (엔지니어 포함)
    @Query("""
    SELECT a FROM Assignment a
    LEFT JOIN FETCH a.engineer
    WHERE a.asRequestId = :asRequestId
    """)
    Optional<Assignment> findByAsRequestIdWithEngineer(@Param("asRequestId") Long asRequestId);
}
