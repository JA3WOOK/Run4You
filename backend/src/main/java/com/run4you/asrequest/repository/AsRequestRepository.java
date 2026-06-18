package com.run4you.asrequest.repository;

import com.run4you.asrequest.entity.AsRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AsRequestRepository extends JpaRepository<AsRequest, Long> {

    // 수리 이력 조회 모달 - 기자재별 수리 이력 전체 조회 (최신순, 완료된 것만)
    @Query("""
            SELECT a FROM AsRequest a
            WHERE a.equipment.id = :equipmentId
            AND a.status = 'COMPLETED'
            ORDER BY a.requestedAt DESC
            """)
    List<AsRequest> findCompletedByEquipmentId(@Param("equipmentId") Long equipmentId);

    // 수리 이력 조회 모달 하단 - 기자재별 총 수리 횟수
    @Query("""
            SELECT COUNT(a) FROM AsRequest a
            WHERE a.equipment.id = :equipmentId
            AND a.status = 'COMPLETED'
            """)
    int countCompletedByEquipmentId(@Param("equipmentId") Long equipmentId);
}
