package com.run4you.report.repository;

import com.run4you.asrequest.dto.ReceiptDetailResponseDto;
import com.run4you.report.entity.RepairReportParts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RepairReportPartsRepository extends JpaRepository<RepairReportParts, Long> {

    // 팀원(asrequest) 영수증 상세 - 교체 부품 목록.
    // 우리 엔티티는 part_code/part_name 을 비정규화 저장하므로 Parts 조인 없이 바로 투영한다.
    @Query("""
            SELECT new com.run4you.asrequest.dto.ReceiptDetailResponseDto$PartItemDto(
                rrp.partCode,
                rrp.partName,
                rrp.quantity,
                rrp.appliedPrice,
                null
            )
            FROM RepairReportParts rrp
            WHERE rrp.report.id = :reportId
            """)
    List<ReceiptDetailResponseDto.PartItemDto> findPartsByReportId(@Param("reportId") Long reportId);
}
