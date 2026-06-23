package com.run4you.report.repository;

import com.run4you.report.entity.RepairReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface RepairReportRepository extends JpaRepository<RepairReport, Long> {

    // ===== 5번 역할(리포트/정산/진단서/대시보드)용 =====
    boolean existsByAssignmentId(Long assignmentId);

    Optional<RepairReport> findByAssignmentId(Long assignmentId);

    List<RepairReport> findByEngineerIdOrderByCreatedAtDesc(Long engineerId);

    List<RepairReport> findAllByOrderByCreatedAtDesc();

    // 진단서 §20 산정용 — 기자재별 수리 횟수 / 리포트 목록(교체 부품 수 집계)
    long countByEquipmentId(Long equipmentId);

    List<RepairReport> findByEquipmentId(Long equipmentId);

    // ===== 팀원(asrequest·equipment) 기능이 사용하는 메서드 =====
    // 수리 이력 조회 모달 - 접수별 리포트
    @Query("SELECT r FROM RepairReport r WHERE r.asRequestId = :asRequestId")
    Optional<RepairReport> findByAsRequestId(@Param("asRequestId") Long asRequestId);

    // 기자재별 총 수리 비용 합계
    @Query("""
            SELECT COALESCE(SUM(r.totalCost), 0) FROM RepairReport r
            JOIN AsRequest a ON a.id = r.asRequestId
            WHERE a.equipment.id = :equipmentId
            """)
    BigDecimal sumTotalCostByEquipmentId(@Param("equipmentId") Long equipmentId);

    // N+1 방지 - 접수 ID 목록으로 한번에 조회
    @Query("SELECT r FROM RepairReport r WHERE r.asRequestId IN :asRequestIds")
    List<RepairReport> findByAsRequestIds(@Param("asRequestIds") List<Long> asRequestIds);
}
