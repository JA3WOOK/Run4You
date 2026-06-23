package com.run4you.report.entity;

import jakarta.persistence.*;
import lombok.Getter;
import java.math.BigDecimal;

@Entity
@Getter
@Table(name = "repair_reports")
public class RepairReport { // 테스트를 위한 임시 구현

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 배정 ID (assignments 테이블 참조)
    @Column(name = "assignment_id")
    private Long assignmentId;

    // 접수 ID (as_requests 테이블 참조)
    @Column(name = "as_request_id")
    private Long asRequestId;

    // 공임비
    @Column(precision = 12, scale = 2)
    private BigDecimal laborCost;

    // 부품비 합계
    @Column(precision = 12, scale = 2)
    private BigDecimal partsCost;

    // 총비용
    @Column(precision = 12, scale = 2)
    private BigDecimal totalCost;

    // 정비 의견
    @Column(columnDefinition = "TEXT")
    private String diagnosis;
}