package com.run4you.matching.service;

import com.run4you.common.enums.AvailabilityStatus;
import com.run4you.matching.entity.EngineerProfile;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * ┌──────────────────────────────────────────────────────────────────────┐
 * │  엔지니어 배정 가중치 스코어링 엔진                                  │
 * │                                                                      │
 * │  종합점수 = 0.30·거리 + 0.25·전문분야 + 0.20·평점                   │
 * │           + 0.15·가용성 + 0.10·긴급도                               │
 * │                                                                      │
 * │  각 항목은 0~100으로 정규화 후 가중합 산출                           │
 * └──────────────────────────────────────────────────────────────────────┘
 */
@Component
public class ScoringEngine {

    // ─── 가중치 상수 ─────────────────────────────────────────────────
    private static final double WEIGHT_DISTANCE    = 0.30;
    private static final double WEIGHT_SPECIALTY   = 0.25;
    private static final double WEIGHT_RATING      = 0.20;
    private static final double WEIGHT_AVAILABILITY = 0.15;
    private static final double WEIGHT_URGENCY     = 0.10;

    /** 거리 점수 기준 최대 반경 (km) */
    private static final double R_MAX_KM = 10.0;

    // ─── 전문분야 점수 상수 ──────────────────────────────────────────
    private static final double SPECIALTY_EXACT   = 100.0;  // 정확히 일치
    private static final double SPECIALTY_RELATED =  60.0;  // 관련 분야
    private static final double SPECIALTY_NONE    =  20.0;  // 미보유

    // ─── 지구 반지름 (Haversine 공식용) ─────────────────────────────
    private static final double EARTH_RADIUS_KM = 6371.0;

    // ─────────────────────────────────────────────────────────────────
    //  메인 스코어 계산
    // ─────────────────────────────────────────────────────────────────

    /**
     * 엔지니어 1명에 대한 전체 스코어 산출
     *
     * @param engineer         엔지니어 프로필 (전문분야 포함)
     * @param storeLat         매장 위도
     * @param storeLng         매장 경도
     * @param requestCategory  접수 기자재 카테고리 (KIOSK / ESPRESSO / ICE_MAKER / REFRIGERATOR)
     * @param priority         접수 우선순위 (EMERGENCY / NORMAL)
     * @param activeCount      현재 진행 중인 배정 건수
     * @return 항목별 점수 + 종합점수를 담은 결과 객체
     */
    public ScoreResult score(
            EngineerProfile engineer,
            double storeLat, double storeLng,
            String requestCategory,
            String priority,
            int activeCount
    ) {
        double distLat  = engineer.getCurrentLatitude().doubleValue();
        double distLng  = engineer.getCurrentLongitude().doubleValue();
        double distKm   = haversine(storeLat, storeLng, distLat, distLng);

        // 반경 외 엔지니어는 대기열 제외 (0점이 아닌 -1 반환)
        if (distKm > engineer.getServiceRadiusKm()) {
            return ScoreResult.outOfRange(engineer, distKm);
        }

        double distScore    = calcDistanceScore(distKm);
        double specScore    = calcSpecialtyScore(engineer, requestCategory);
        double ratingScore  = calcRatingScore(engineer.getRating().doubleValue());
        double availScore   = calcAvailabilityScore(engineer.getAvailabilityStatus(),
                                                    activeCount,
                                                    engineer.getDailyCapacity());
        double urgScore     = calcUrgencyScore(priority, engineer.getAvailabilityStatus());

        double total = WEIGHT_DISTANCE    * distScore
                     + WEIGHT_SPECIALTY   * specScore
                     + WEIGHT_RATING      * ratingScore
                     + WEIGHT_AVAILABILITY * availScore
                     + WEIGHT_URGENCY     * urgScore;

        // ETA = 거리(km) / 평균속도(30km/h) × 60분
        int etaMinutes = (int) Math.ceil(distKm / 30.0 * 60.0);

        return new ScoreResult(
                engineer, distKm, etaMinutes,
                round(distScore), round(specScore), round(ratingScore),
                round(availScore), round(urgScore), round(total),
                true
        );
    }

    // ─────────────────────────────────────────────────────────────────
    //  항목별 산식
    // ─────────────────────────────────────────────────────────────────

    /**
     * 거리 점수 = (1 - d / R_max) × 100
     * d ≥ R_max 이면 0
     */
    double calcDistanceScore(double distKm) {
        if (distKm >= R_MAX_KM) return 0.0;
        return (1.0 - distKm / R_MAX_KM) * 100.0;
    }

    /**
     * 전문분야 점수
     * - 보유(정확 일치): 100
     * - 관련 분야(예: ESPRESSO ↔ ICE_MAKER): 60
     * - 미보유: 20
     *
     * 현재 MVP에서는 단순 일치 여부로 판단. 관련 분야는 하드코딩 매핑.
     */
    double calcSpecialtyScore(EngineerProfile engineer, String requestCategory) {
        boolean hasExact = engineer.getSpecialties().stream()
                .anyMatch(s -> s.getCategory().equals(requestCategory));
        if (hasExact) return SPECIALTY_EXACT;

        boolean hasRelated = hasRelatedSpecialty(engineer, requestCategory);
        return hasRelated ? SPECIALTY_RELATED : SPECIALTY_NONE;
    }

    /**
     * 평점 점수 = (rating / 5) × 100
     */
    double calcRatingScore(double rating) {
        return Math.min((rating / 5.0) * 100.0, 100.0);
    }

    /**
     * 가용성 점수 = (1 - active / capacity) × 100
     * OFFLINE → 0
     */
    double calcAvailabilityScore(AvailabilityStatus status, int activeCount, int dailyCapacity) {
        if (status == AvailabilityStatus.OFFLINE) return 0.0;
        if (dailyCapacity <= 0) return 0.0;
        double ratio = (double) activeCount / dailyCapacity;
        return Math.max(0.0, (1.0 - ratio) * 100.0);
    }

    /**
     * 긴급도 점수 — 우선순위 × 가용상태 매트릭스
     *
     * | 우선순위 \ 가용 | AVAILABLE | BUSY | OFFLINE |
     * |---|---|---|---|
     * | EMERGENCY | 100 | 50 | 0 |
     * | NORMAL    |  50 | 25 | 0 |
     */
    double calcUrgencyScore(String priority, AvailabilityStatus availabilityStatus) {
        if (availabilityStatus == AvailabilityStatus.OFFLINE) return 0.0;

        boolean isEmergency = "EMERGENCY".equals(priority);
        boolean isAvailable = availabilityStatus == AvailabilityStatus.AVAILABLE;

        if (isEmergency) return isAvailable ? 100.0 : 50.0;
        else             return isAvailable ?  50.0 : 25.0;
    }

    // ─────────────────────────────────────────────────────────────────
    //  Haversine 공식 — 두 좌표 간 구면 거리 (km)
    // ─────────────────────────────────────────────────────────────────

    /**
     * Haversine formula
     * d = 2R × arcsin(√(sin²(Δlat/2) + cos(lat1)·cos(lat2)·sin²(Δlng/2)))
     */
    public double haversine(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                 + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                 * Math.sin(dLng / 2) * Math.sin(dLng / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }

    // ─────────────────────────────────────────────────────────────────
    //  보조 유틸
    // ─────────────────────────────────────────────────────────────────

    /** 관련 분야 매핑 (MVP 하드코딩, 추후 DB화 가능) */
    private boolean hasRelatedSpecialty(EngineerProfile engineer, String requestCategory) {
        // 예: ICE_MAKER ↔ REFRIGERATOR (냉각 계열)
        //     KIOSK ↔ (없음)
        //     ESPRESSO ↔ ICE_MAKER (카페 기자재 계열)
        var relatedMap = java.util.Map.of(
                "ICE_MAKER",    java.util.Set.of("REFRIGERATOR", "ESPRESSO"),
                "REFRIGERATOR", java.util.Set.of("ICE_MAKER"),
                "ESPRESSO",     java.util.Set.of("ICE_MAKER"),
                "KIOSK",        java.util.Set.<String>of()
        );
        var related = relatedMap.getOrDefault(requestCategory, java.util.Set.of());
        return engineer.getSpecialties().stream()
                .anyMatch(s -> related.contains(s.getCategory()));
    }

    private double round(double value) {
        return BigDecimal.valueOf(value)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    // ─────────────────────────────────────────────────────────────────
    //  스코어 결과 VO
    // ─────────────────────────────────────────────────────────────────

    @Getter
    public static class ScoreResult {
        private final EngineerProfile engineer;
        private final double distanceKm;
        private final int etaMinutes;

        private final double distanceScore;
        private final double specialtyScore;
        private final double ratingScore;
        private final double availabilityScore;
        private final double urgencyScore;
        private final double totalScore;

        /** 반경 내 유효 후보 여부 */
        private final boolean inRange;

        public ScoreResult(
                EngineerProfile engineer, double distanceKm, int etaMinutes,
                double distanceScore, double specialtyScore, double ratingScore,
                double availabilityScore, double urgencyScore, double totalScore,
                boolean inRange
        ) {
            this.engineer        = engineer;
            this.distanceKm      = distanceKm;
            this.etaMinutes      = etaMinutes;
            this.distanceScore   = distanceScore;
            this.specialtyScore  = specialtyScore;
            this.ratingScore     = ratingScore;
            this.availabilityScore = availabilityScore;
            this.urgencyScore    = urgencyScore;
            this.totalScore      = totalScore;
            this.inRange         = inRange;
        }

        static ScoreResult outOfRange(EngineerProfile engineer, double distanceKm) {
            return new ScoreResult(engineer, distanceKm, 0,
                    0, 0, 0, 0, 0, 0, false);
        }
    }
}
