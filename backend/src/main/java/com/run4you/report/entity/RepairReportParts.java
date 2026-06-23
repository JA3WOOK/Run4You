package com.run4you.report.entity;

import jakarta.persistence.*;
import lombok.Getter;
import java.math.BigDecimal;

@Entity
@Getter
@Table(name = "repair_report_parts")
public class RepairReportParts { // 테스트용 임시 구현

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "report_id")
    private Long reportId;

    @Column(name = "part_id")
    private Long partId;

    private Integer quantity;

    @Column(name = "applied_price", precision = 12, scale = 2)
    private BigDecimal appliedPrice;
}