package com.run4you.report.repository;

import com.run4you.asrequest.dto.ReceiptDetailResponseDto;
import com.run4you.report.entity.RepairReportParts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RepairReportPartsRepository extends JpaRepository<RepairReportParts, Long> {

    // 상세 - 교체 부품 목록
    @Query("""
        SELECT new com.run4you.asrequest.dto.ReceiptDetailResponseDto$PartItemDto(
            p.partCode,
            p.name,
            rrp.quantity,
            rrp.appliedPrice,
            null
        )
        FROM RepairReportParts rrp
        LEFT JOIN Parts p ON p.id = rrp.partId
        WHERE rrp.reportId = :reportId
        """)
    List<ReceiptDetailResponseDto.PartItemDto> findPartsByReportId(@Param("reportId") Long reportId);

}
