package com.run4you.matching.dto;

import com.run4you.matching.service.ScoringEngine.ScoreResult;
import com.run4you.asrequest.entity.AsRequest;
import lombok.Builder;
import lombok.Getter;

import java.time.format.DateTimeFormatter;

/**
  출동 요청 대기열 — 엔지니어 화면 아이템

  엔지니어 개인 스코어 기준으로 정렬된 AS 요청 목록
 */
@Getter
@Builder
public class MatchingQueueItemResponse {

    // 대기열 순위
    private int rank;

    // ─── AS 요청 정보 ─────────────────────────────────────────────
    private Long   asRequestId;
    private String asRequestNo;     // AS-2026-XXXX
    private String storeName;
    private String storeDistrict;   // 강남구 역삼동
    private String priority;        // EMERGENCY / NORMAL
    private String errorCode;       // PUMP_FAIL 등
    private String equipmentType;   // 에스프레소 머신 (카테고리)
    private String equipmentModel;  // La Marzocco
    private String receivedTime;    // 14:28

    // ─── 거리 / ETA ──────────────────────────────────────────────
    private double distanceKm;
    private int    etaMinutes;

    // ─── 종합 점수 ───────────────────────────────────────────────
    private double totalScore;

    // ─── 항목별 점수 (0~100, 프로그레스 바용) ────────────────────
    private double distanceScore;
    private double specialtyScore;
    private double ratingScore;
    private double availabilityScore;
    private double urgencyScore;

    // ─── 가중치 레이블 (UI 고정값) ───────────────────────────────
    public int getDistanceWeight()     { return 30; }
    public int getSpecialtyWeight()    { return 25; }
    public int getRatingWeight()       { return 20; }
    public int getAvailabilityWeight() { return 15; }
    public int getUrgencyWeight()      { return 10; }

    // ─── 팩토리 ──────────────────────────────────────────────────

    public static MatchingQueueItemResponse of(int rank, AsRequest req, ScoreResult score) {
        var equipment = req.getEquipment();
        var store     = req.getStore();

        return MatchingQueueItemResponse.builder()
                .rank(rank)
                .asRequestId(req.getId())
                .asRequestNo("AS-" + req.getRequestedAt().getYear() + "-" + String.format("%04d", req.getId()))
                .storeName(store.getName())
                .storeDistrict(extractDistrict(store.getAddress()))    // address 파싱
                .priority(req.getPriority().name())
                .errorCode(req.getErrorCode())
                .equipmentType(equipment.getCategory().name())
                .equipmentModel(equipment.getModelName())
                .receivedTime(req.getRequestedAt().format(DateTimeFormatter.ofPattern("HH:mm")))
                .distanceKm(score.getDistanceKm())
                .etaMinutes(score.getEtaMinutes())
                .totalScore(score.getTotalScore())
                .distanceScore(score.getDistanceScore())
                .specialtyScore(score.getSpecialtyScore())
                .ratingScore(score.getRatingScore())
                .availabilityScore(score.getAvailabilityScore())
                .urgencyScore(score.getUrgencyScore())
                .build();
    }

    /**
     * 주소에서 "구 동" 추출
     * "서울특별시 강남구 역삼동 123-45" → "강남구 역삼동"
     * "서울 마포구 합정로 10"          → "마포구 합정로"
     */
    private static String extractDistrict(String address) {
        if (address == null || address.isBlank()) return "";

        String[] parts = address.split(" ");
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (part.endsWith("구") || part.endsWith("군")) {
                // "강남구" 다음 단어("역삼동")까지 합쳐서 반환
                if (i + 1 < parts.length) {
                    return part + " " + parts[i + 1];
                }
                return part;
            }
        }
        // 구/군을 못 찾으면 주소 그대로 반환
        return address;
    }
}
