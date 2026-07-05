package com.run4you.lms.dto;

import java.util.List;

/**
 * 관리자 교육 이수 현황 응답.
 * 상단 요약(summary) + 엔지니어별 행(engineers) 두 블록으로 구성한다.
 */
public record TrainingStatusResponse(
        Summary summary,
        List<EngineerTrainingRow> engineers
) {
    /**
     * 플랫폼 전체 집계.
     * completionRate = 이수(COMPLETED) 수강 / 전체 수강 등록 (%)
     * passRate       = 합격 시험 / 응시 시험 (%)
     */
    public record Summary(
            int totalEngineers,
            long totalEnrolled,
            long totalCompleted,
            double completionRate,
            long totalAttempted,
            long totalPassed,
            double passRate
    ) {}
}
