package com.run4you.matching.dto;

import com.run4you.matching.entity.AssignmentCandidate;
import lombok.Builder;
import lombok.Getter;

/**
  배정 후보 스코어 로그 — BRAND_ADMIN 관제 화면 (배정 근거 추적용)
  GET /api/assignments/{asRequestId}/candidates
 */
@Getter
@Builder
public class CandidateScoreResponse {

    private Long   engineerId;
    private String engineerName;
    private boolean selected;

    private double distanceScore;
    private double specialtyScore;
    private double ratingScore;
    private double availabilityScore;
    private double urgencyScore;
    private double totalScore;

    public static CandidateScoreResponse from(AssignmentCandidate candidate) {
        return CandidateScoreResponse.builder()
                .engineerId(candidate.getEngineer().getId())
                .engineerName(candidate.getEngineer().getName())
                .selected(candidate.isSelected())
                .distanceScore(candidate.getDistanceScore().doubleValue())
                .specialtyScore(candidate.getSpecialtyScore().doubleValue())
                .ratingScore(candidate.getRatingScore().doubleValue())
                .availabilityScore(candidate.getAvailabilityScore().doubleValue())
                .urgencyScore(candidate.getUrgencyScore().doubleValue())
                .totalScore(candidate.getTotalScore().doubleValue())
                .build();
    }
}
