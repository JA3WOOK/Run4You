package com.run4you.equipment.repository;

import com.run4you.equipment.entity.Equipment;
import com.run4you.equipment.entity.EquipmentCategory;
import com.run4you.equipment.entity.EquipmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EquipmentRepository extends JpaRepository<Equipment, Long> {

    // 해당 매장 기자재 전체 조회
    @Query("SELECT e FROM Equipment e WHERE e.store.id = :storeId AND e.deletedAt IS NULL")
    List<Equipment> findActiveByStoreId(@Param("storeId") Long storeId);

    // 매장별 카테고리 필터 조회
    @Query("""
            SELECT e FROM Equipment e
            WHERE e.store.id = :storeId
            AND e.category = :category
            AND e.deletedAt IS NULL
            """)
    List<Equipment> findActiveByStoreIdAndCategory(
            @Param("storeId") Long storeId,
            @Param("category") EquipmentCategory category);

    // 매장별 키워드 검색 (기기명/모델명/시리얼번호)
    @Query("""
            SELECT e FROM Equipment e
            WHERE e.store.id = :storeId AND e.deletedAt IS NULL
            AND (e.name LIKE %:keyword%
                OR e.modelName LIKE %:keyword%
                OR e.serialNo LIKE %:keyword%)
            """)
    List<Equipment> findActiveByStoreIdAndKeyword(
            @Param("storeId") Long storeId,
            @Param("keyword") String keyword);

    // 매장별 카테고리 필터 + 키워드 검색 동시
    @Query("""
        SELECT e FROM Equipment e
        WHERE e.store.id = :storeId
        AND e.category = :category
        AND e.deletedAt IS NULL
        AND (e.name LIKE %:keyword%
            OR e.modelName LIKE %:keyword%
            OR e.serialNo LIKE %:keyword%)
        """)
    List<Equipment> findActiveByStoreIdAndCategoryAndKeyword(
            @Param("storeId") Long storeId,
            @Param("category") EquipmentCategory category,
            @Param("keyword") String keyword);

    // 상태별 카운트
    @Query("""
            SELECT COUNT(e) FROM Equipment e
            WHERE e.store.id = :storeId 
            AND e.status = :status AND e.deletedAt IS NULL
            """)
    int countByStoreIdAndStatus(
            @Param("storeId") Long storeId,
            @Param("status") EquipmentStatus status);

    // 매장병 전체 카운트
    @Query("SELECT COUNT(e) FROM Equipment e WHERE e.store.id = :storeId AND e.deletedAt IS NULL")
    int countByActiveByStoreId(@Param("storeId") Long storeId);
}
