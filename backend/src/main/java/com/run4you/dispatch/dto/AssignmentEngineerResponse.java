package com.run4you.dispatch.dto;

import java.math.BigDecimal;

/**
 * 배정에 연결된 엔지니어의 표시용 정보.
 * 점주 추적 화면 엔지니어 카드의 평점·기술등급을 실데이터로 채우기 위한 읽기 전용 뷰.
 */
public record AssignmentEngineerResponse(
        Long engineerId,
        String name,
        String phone,
        BigDecimal rating,      // engineer_profiles.rating (0.00~5.00)
        Integer ratingCount,    // 평가 수
        String skillGrade       // BEGINNER / INTERMEDIATE / ADVANCED
) {}
