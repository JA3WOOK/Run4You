package com.run4you.asrequest.repository;

import com.run4you.asrequest.dto.ReceiptListResponseDto;
import com.run4you.asrequest.entity.AsRequest;
import com.run4you.asrequest.entity.AsStatus;
import com.run4you.dispatch.domain.DispatchStatus;
import com.run4you.equipment.entity.EquipmentCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AsRequestRepository extends JpaRepository<AsRequest, Long> {

    // 수락 가능한 AS 요청 전체 조회 (매칭 대기열용)
    // Store, Equipment 조인 페치로 N+1 방지
    @Query("""
            SELECT ar FROM AsRequest ar
            JOIN FETCH ar.store s
            JOIN FETCH ar.equipment e
            WHERE ar.status = 'RECEIVED'
            ORDER BY ar.priority DESC, ar.requestedAt ASC
            """)
    List<AsRequest> findAllReceived();

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

    // A/S 접수 현황 - 중복 접수 방지용
    @Query("""
        SELECT COUNT(a) > 0 FROM AsRequest a
        WHERE a.equipment.id = :equipmentId
        AND a.status NOT IN ('COMPLETED', 'CANCELLED')
        """)
    boolean existsActiveByEquipmentId(@Param("equipmentId") Long equipmentId);

    // 진단서 및 영수증 목록 조회 (날짜 필터 + 카테고리)
    @Query("""
        SELECT new com.run4you.asrequest.dto.ReceiptListResponseDto$ReceiptItemDto(
            a.id,
            a.requestedAt,
            a.status,
            a.equipment.name,
            a.equipment.modelName,
            rr.diagnosis,
            asn.engineer.name,
            (SELECT MIN(dsh.changedAt) FROM DispatchStatusHistory dsh
                WHERE dsh.assignmentId = asn.id
                AND dsh.status = :repairingStatus),
            asn.completedAt,
            rr.totalCost
        )
        FROM AsRequest a
        LEFT JOIN Assignment asn ON asn.asRequest.id = a.id
        LEFT JOIN RepairReport rr ON rr.assignmentId = asn.id
        WHERE a.requester.id = :requesterId
          AND a.status = :status
          AND (:startDate IS NULL OR a.requestedAt >= :startDate)
          AND (:endDate IS NULL OR a.requestedAt <= :endDate)
          AND (:category IS NULL OR a.equipment.category = :category)
        ORDER BY a.requestedAt DESC
        """)
    List<ReceiptListResponseDto.ReceiptItemDto> findReceiptsByRequesterId(
            @Param("requesterId") Long requesterId,
            @Param("status") AsStatus status,
            @Param("repairingStatus") DispatchStatus repairingStatus,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("category") EquipmentCategory category);

    // 기자재 ID로 진행 중인 접수 1건 조회 (고장 기자재의 접수 내용 보기용)
    @Query("""
        SELECT a FROM AsRequest a
        WHERE a.equipment.id = :equipmentId
        AND a.status NOT IN ('COMPLETED', 'CANCELLED')
        ORDER BY a.requestedAt DESC
        LIMIT 1
        """)
    Optional<AsRequest> findActiveByEquipmentId(@Param("equipmentId") Long equipmentId);
}
