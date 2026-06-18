package com.run4you.report.repository;

import com.run4you.report.entity.RepairReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;

public interface RepairReportRepository extends JpaRepository<RepairReport, Long> {

    // 수리 이력 조회 모달 - 수리 비용 + 정비 의견 띄우기
    @Query("SELECT r FROM RepairReport r WHERE r.asRequestId = :asRequestId")
    Optional<RepairReport> findByAsRequestId(@Param("asRequestId") Long asRequestId);

    // 수리 이력 조회 모달 - 기자재별 총 수리 비용 합계 출력
    @Query("""
            SELECT COALESCE(SUM(r.totalCost), 0) FROM RepairReport r
            JOIN AsRequest a ON a.id = r.asRequestId
            WHERE a.equipment.id = :equipmentId
            """)
    BigDecimal sumTotalCostByEquipmentId(@Param("equipmentId") Long equipmentId);
}
