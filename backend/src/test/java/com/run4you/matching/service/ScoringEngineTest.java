package com.run4you.matching.service;

import com.run4you.common.enums.AvailabilityStatus;
import com.run4you.matching.entity.EngineerProfile;
import com.run4you.matching.entity.EngineerSpecialty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * ScoringEngine 단위 테스트
    엔지니어 A: 거리 2km, 키오스크 보유, 평점 4.6, active 1/5, AVAILABLE
   → 종합점수 89.4
 */
class ScoringEngineTest {

    private ScoringEngine engine;

    @BeforeEach
    void setUp() { engine = new ScoringEngine(); }

    // ─────────────────────────────────────────────────────────────────
    //  거리 점수
    // ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("거리 0km → 100점")
    void distanceScore_zero() {
        assertThat(engine.calcDistanceScore(0.0)).isEqualTo(100.0);
    }

    @Test
    @DisplayName("거리 5km → 50점 (R_max 10km)")
    void distanceScore_half() {
        assertThat(engine.calcDistanceScore(5.0)).isEqualTo(50.0);
    }

    @Test
    @DisplayName("거리 10km 이상 → 0점")
    void distanceScore_outOfRange() {
        assertThat(engine.calcDistanceScore(10.0)).isEqualTo(0.0);
        assertThat(engine.calcDistanceScore(15.0)).isEqualTo(0.0);
    }

    @Test
    @DisplayName("거리 2km → 80점 (기획서 예시 A)")
    void distanceScore_engineerA() {
        assertThat(engine.calcDistanceScore(2.0)).isEqualTo(80.0);
    }

    // ─────────────────────────────────────────────────────────────────
    //  전문분야 점수
    // ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("보유 전문분야 일치 → 100점")
    void specialtyScore_exact() {
        EngineerProfile ep = buildEngineer(AvailabilityStatus.AVAILABLE, 5.0, 5, 1,
                List.of("KIOSK"));
        assertThat(engine.calcSpecialtyScore(ep, "KIOSK")).isEqualTo(100.0);
    }

    @Test
    @DisplayName("미보유(관련 없음) → 20점")
    void specialtyScore_none() {
        EngineerProfile ep = buildEngineer(AvailabilityStatus.AVAILABLE, 5.0, 5, 1,
                List.of("KIOSK"));
        assertThat(engine.calcSpecialtyScore(ep, "ESPRESSO")).isEqualTo(20.0);
    }

    @Test
    @DisplayName("관련 분야(ICE_MAKER→REFRIGERATOR) → 60점")
    void specialtyScore_related() {
        EngineerProfile ep = buildEngineer(AvailabilityStatus.AVAILABLE, 5.0, 5, 1,
                List.of("ICE_MAKER"));
        assertThat(engine.calcSpecialtyScore(ep, "REFRIGERATOR")).isEqualTo(60.0);
    }

    // ─────────────────────────────────────────────────────────────────
    //  평점 점수
    // ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("평점 4.6 → 92점")
    void ratingScore_4_6() {
        assertThat(engine.calcRatingScore(4.6)).isEqualTo(92.0);
    }

    @Test
    @DisplayName("평점 5.0 → 100점")
    void ratingScore_max() {
        assertThat(engine.calcRatingScore(5.0)).isEqualTo(100.0);
    }

    @Test
    @DisplayName("평점 0 → 0점")
    void ratingScore_zero() {
        assertThat(engine.calcRatingScore(0.0)).isEqualTo(0.0);
    }

    // ─────────────────────────────────────────────────────────────────
    //  가용성 점수
    // ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("OFFLINE → 0점")
    void availabilityScore_offline() {
        assertThat(engine.calcAvailabilityScore(AvailabilityStatus.OFFLINE, 0, 5)).isEqualTo(0.0);
    }

    @Test
    @DisplayName("active 1/5, AVAILABLE → 80점")
    void availabilityScore_oneOfFive() {
        assertThat(engine.calcAvailabilityScore(AvailabilityStatus.AVAILABLE, 1, 5)).isEqualTo(80.0);
    }

    @Test
    @DisplayName("active 5/5(포화) → 0점")
    void availabilityScore_full() {
        assertThat(engine.calcAvailabilityScore(AvailabilityStatus.BUSY, 5, 5)).isEqualTo(0.0);
    }

    // ─────────────────────────────────────────────────────────────────
    //  긴급도 매트릭스
    // ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("EMERGENCY + AVAILABLE → 100점")
    void urgencyScore_emergencyAvailable() {
        assertThat(engine.calcUrgencyScore("EMERGENCY", AvailabilityStatus.AVAILABLE)).isEqualTo(100.0);
    }

    @Test
    @DisplayName("EMERGENCY + BUSY → 50점")
    void urgencyScore_emergencyBusy() {
        assertThat(engine.calcUrgencyScore("EMERGENCY", AvailabilityStatus.BUSY)).isEqualTo(50.0);
    }

    @Test
    @DisplayName("NORMAL + AVAILABLE → 50점")
    void urgencyScore_normalAvailable() {
        assertThat(engine.calcUrgencyScore("NORMAL", AvailabilityStatus.AVAILABLE)).isEqualTo(50.0);
    }

    @Test
    @DisplayName("NORMAL + BUSY → 25점")
    void urgencyScore_normalBusy() {
        assertThat(engine.calcUrgencyScore("NORMAL", AvailabilityStatus.BUSY)).isEqualTo(25.0);
    }

    // ─────────────────────────────────────────────────────────────────
    //  종합 점수 — 기획서 §18.4 예시 검증
    // ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("기획서 엔지니어 A 종합점수 = 89.4")
    void totalScore_engineerA() {
        // 거리 2km, KIOSK 보유, 평점 4.6, active 1/5, AVAILABLE, EMERGENCY
        EngineerProfile engineerA = buildEngineer(AvailabilityStatus.AVAILABLE, 4.6, 5, 1,
                List.of("KIOSK"));
        engineerA.getSpecialties(); // 전문분야 로드 확인

        double dist     = engine.calcDistanceScore(2.0);    // 80
        double spec     = engine.calcSpecialtyScore(engineerA, "KIOSK"); // 100
        double rating   = engine.calcRatingScore(4.6);      // 92
        double avail    = engine.calcAvailabilityScore(AvailabilityStatus.AVAILABLE, 1, 5); // 80
        double urgency  = engine.calcUrgencyScore("EMERGENCY", AvailabilityStatus.AVAILABLE); // 100

        double total = 0.30 * dist + 0.25 * spec + 0.20 * rating + 0.15 * avail + 0.10 * urgency;
        // = 24 + 25 + 18.4 + 12 + 10 = 89.4

        assertThat(total).isCloseTo(89.4, within(0.01));
    }

    @Test
    @DisplayName("기획서 엔지니어 B 종합점수 = 60.6")
    void totalScore_engineerB() {
        // 거리 5km, 관련분야(ESPRESSO → KIOSK 미보유), 평점 4.9, active 3/5, BUSY, EMERGENCY
        double dist     = engine.calcDistanceScore(5.0);    // 50
        double spec     = 60.0;                             // 관련분야
        double rating   = engine.calcRatingScore(4.9);      // 98
        double avail    = engine.calcAvailabilityScore(AvailabilityStatus.BUSY, 3, 5); // 40
        double urgency  = engine.calcUrgencyScore("EMERGENCY", AvailabilityStatus.BUSY); // 50

        double total = 0.30 * dist + 0.25 * spec + 0.20 * rating + 0.15 * avail + 0.10 * urgency;
        // = 15 + 15 + 19.6 + 6 + 5 = 60.6

        assertThat(total).isCloseTo(60.6, within(0.01));
    }

    @Test
    @DisplayName("Haversine: 서울 강남↔서초 약 2km")
    void haversine_gangnamSeocho() {
        // 강남구 역삼동 37.5007, 127.0367
        // 서초구 서초동 37.4923, 127.0073
        double dist = engine.haversine(37.5007, 127.0367, 37.4923, 127.0073);
        assertThat(dist).isBetween(2.0, 4.0);
    }

    // ─────────────────────────────────────────────────────────────────
    //  테스트용 EngineerProfile 빌더
    // ─────────────────────────────────────────────────────────────────

    private EngineerProfile buildEngineer(
            AvailabilityStatus status,
            double rating,
            int dailyCapacity,
            int serviceRadiusKm,
            List<String> categories
    ) {
        EngineerProfile ep = EngineerProfile.builder()
                .rating(BigDecimal.valueOf(rating))
                .availabilityStatus(status)
                .dailyCapacity(dailyCapacity)
                .serviceRadiusKm(serviceRadiusKm)
                .currentLatitude(BigDecimal.valueOf(37.5007))
                .currentLongitude(BigDecimal.valueOf(127.0367))
                .build();

        categories.forEach(cat -> {
            EngineerSpecialty specialty = EngineerSpecialty.of(ep, cat);
            ep.getSpecialties().add(specialty);
        });
        return ep;
    }
}
