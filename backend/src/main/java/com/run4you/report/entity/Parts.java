package com.run4you.report.entity;

import jakarta.persistence.*;
import lombok.Getter;
import java.math.BigDecimal;

@Entity
@Getter
@Table(name = "parts")
public class Parts { // 테스트용 임시 구현

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "part_code")
    private String partCode;

    private String name;

    @Column(name = "unit_price", precision = 12, scale = 2)
    private BigDecimal unitPrice;
}