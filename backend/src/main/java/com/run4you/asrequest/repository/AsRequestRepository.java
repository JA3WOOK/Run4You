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

    // 기자재 목록 카드 - 매장별 기자재의 최근 에러코드 한번에 조회
    @Query("""
        SELECT a FROM AsRequest a
        WHERE a.id IN (
            SELECT MAX(a2.id) FROM AsRequest a2
            WHERE a2.store.id = :storeId
            AND a2.errorCode IS NOT NULL
            GROUP BY a2.equipment.id
        )
        """)
    List<AsRequest> findLatestErrorCodesByStoreId(@Param("storeId") Long storeId);
}
