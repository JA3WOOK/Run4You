package com.run4you.matching.dto;

import com.run4you.asrequest.entity.AsRequest;
import com.run4you.matching.service.ScoringEngine.ScoreResult;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
  출동 상세 정보 — 엔지니어 수락 전 상세 화면

  고장 기자재 정보 + 가중치 배정 점수 + 출동 정보를 포함한다.
 */
@Getter
@Builder
public class AssignmentDetailResponse {

    // ─── AS 요청 정보 ─────────────────────────────────────────────
    private Long   asRequestId;
    private String asRequestNo;
    private String storeName;
    private String storeAddress;
    private String priority;
    private String symptom;
    private String errorCode;

    // ─── 기자재 정보 ─────────────────────────────────────────────
    private String equipmentName;
    private String serialNumber;
    private String purchasedDate;       // purchasedAt (구매일)
    private String lastRepairedDate;
    private String equipmentCategory;

    // ─── 가중치 배정 점수 ────────────────────────────────────────
    private double totalScore;       // 종합 점수 (0~100)
    private double distanceScore;
    private double specialtyScore;
    private double ratingScore;
    private double availabilityScore;
    private double urgencyScore;

    /** 가중치 레이블 */
    public int getDistanceWeight()     { return 30; }
    public int getSpecialtyWeight()    { return 25; }
    public int getRatingWeight()       { return 20; }
    public int getAvailabilityWeight() { return 15; }
    public int getUrgencyWeight()      { return 10; }

    // ─── 출동 정보 ───────────────────────────────────────────────
    private double distanceKm;
    private int    etaMinutes;
    private String trafficCondition;  // 간이 교통 상황 (혼잡/원활 - MVP 고정값)

    // ─── 팩토리 ──────────────────────────────────────────────────

    public static AssignmentDetailResponse of(AsRequest req, ScoreResult score, LocalDateTime lastRepairedAt)  {
        var equipment  = req.getEquipment();
        var store      = req.getStore();
        var dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        return AssignmentDetailResponse.builder()
                .asRequestId(req.getId())
                .asRequestNo("AS-" + req.getRequestedAt().getYear() + "-" + String.format("%04d", req.getId()))
                .storeName(store.getName())
                .storeAddress(store.getAddress())
                .priority(req.getPriority().name())
                .symptom(req.getSymptom())
                .errorCode(req.getErrorCode())
                // 기자재
                .equipmentName(equipment.getName())
                .serialNumber(equipment.getSerialNo())
                .purchasedDate(equipment.getPurchasedAt() != null ? equipment.getPurchasedAt().format(dateFormat) : "-")
                .lastRepairedDate(lastRepairedAt != null ? lastRepairedAt.format(dateFormat) : "-")        // null이면 수리 이력 없음
                .equipmentCategory(equipment.getCategory().name())
                // 점수
                .totalScore(score.getTotalScore())
                .distanceScore(score.getDistanceScore())
                .specialtyScore(score.getSpecialtyScore())
                .ratingScore(score.getRatingScore())
                .availabilityScore(score.getAvailabilityScore())
                .urgencyScore(score.getUrgencyScore())
                // 출동
                .distanceKm(score.getDistanceKm())
                .etaMinutes(score.getEtaMinutes())
                .trafficCondition(resolveTrafficCondition(score.getDistanceKm()))
                .build();
    }

    /** 간이 교통 상황 판단 (MVP: 거리 기반 단순 분류, 2단계에서 T Map API로 교체) */
    private static String resolveTrafficCondition(double distKm) {
        if (distKm < 3.0) return "원활";
        if (distKm < 6.0) return "보통";
        return "혼잡";
    }
}
