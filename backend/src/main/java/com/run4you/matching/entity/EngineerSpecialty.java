package com.run4you.matching.entity;

import jakarta.persistence.*;
import lombok.*;

/**
  엔지니어 전문 분야 — engineer_profiles와 1:N (N:M 해소 테이블)
  배정 스코어링 전문분야 점수(가중치 25%) 산출에 사용된다.
 */
@Entity
@Table(name = "engineer_specialties",
        uniqueConstraints = @UniqueConstraint(name = "uq_eng_specialty", columnNames = {"engineer_id", "category"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class EngineerSpecialty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "engineer_id", nullable = false)
    private EngineerProfile engineerProfile;

    /**
      전문 기자재 카테고리
      KIOSK / ESPRESSO / ICE_MAKER / REFRIGERATOR
     */
    @Column(nullable = false, length = 20)
    private String category;

    public static EngineerSpecialty of(EngineerProfile profile, String category) {
        return EngineerSpecialty.builder()
                .engineerProfile(profile)
                .category(category)
                .build();
    }
}
