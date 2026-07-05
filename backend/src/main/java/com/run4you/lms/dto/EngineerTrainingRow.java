package com.run4you.lms.dto;

import java.time.LocalDateTime;

/**
 * 엔지니어 1명의 교육 이수 요약 행.
 *
 * <ul>
 *   <li>enrolledCount   — 수강 등록한 코스 수 (enrollments)</li>
 *   <li>completedCount  — 진도율 100% 이수(status=COMPLETED)한 코스 수</li>
 *   <li>completionRate  — completedCount / enrolledCount (%), 수강 0건이면 0</li>
 *   <li>attemptedCount  — 응시한 시험(코스) 수 (distinct exam)</li>
 *   <li>passedCount     — 합격(passed=true 이력 존재)한 시험(코스) 수 (distinct exam)</li>
 *   <li>avgProgressRate — 수강 코스들의 평균 진도율</li>
 *   <li>lastAttemptedAt — 가장 최근 시험 응시 시각 (없으면 null)</li>
 * </ul>
 */
public record EngineerTrainingRow(
        Long engineerId,
        String engineerName,
        String skillGrade,
        long enrolledCount,
        long completedCount,
        double completionRate,
        long attemptedCount,
        long passedCount,
        double avgProgressRate,
        LocalDateTime lastAttemptedAt
) {}
