package com.run4you.lms.service;

import com.run4you.lms.dto.EngineerTrainingRow;
import com.run4you.lms.dto.TrainingStatusResponse;
import com.run4you.lms.repository.TrainingStatsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 관리자(플랫폼 총괄) 교육 이수 현황 조회 서비스.
 * 엔지니어별 집계 행을 받아 플랫폼 전체 요약을 합산한다.
 */
@Service
@RequiredArgsConstructor
public class AdminTrainingService {

    private final TrainingStatsRepository trainingStatsRepository;

    @Transactional(readOnly = true)
    public TrainingStatusResponse getTrainingStatus() {
        List<EngineerTrainingRow> rows = trainingStatsRepository.findEngineerTrainingRows();

        int totalEngineers = rows.size();
        long totalEnrolled  = rows.stream().mapToLong(EngineerTrainingRow::enrolledCount).sum();
        long totalCompleted = rows.stream().mapToLong(EngineerTrainingRow::completedCount).sum();
        long totalAttempted = rows.stream().mapToLong(EngineerTrainingRow::attemptedCount).sum();
        long totalPassed    = rows.stream().mapToLong(EngineerTrainingRow::passedCount).sum();

        TrainingStatusResponse.Summary summary = new TrainingStatusResponse.Summary(
                totalEngineers,
                totalEnrolled,
                totalCompleted,
                rate(totalCompleted, totalEnrolled),
                totalAttempted,
                totalPassed,
                rate(totalPassed, totalAttempted)
        );

        return new TrainingStatusResponse(summary, rows);
    }

    /** 소수점 첫째 자리까지 반올림한 백분율. 분모 0이면 0. */
    private double rate(long numerator, long denominator) {
        if (denominator == 0) return 0.0;
        return Math.round(numerator * 1000.0 / denominator) / 10.0;
    }
}
