package com.run4you.matching.entity;

import jakarta.persistence.*;
import lombok.Getter;
import java.math.BigDecimal;

@Entity
@Getter
@Table(name = "engineer_profiles")
public class EngineerProfile { // 테스트용 임시 구현

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(precision = 3, scale = 2)
    private BigDecimal rating;
}