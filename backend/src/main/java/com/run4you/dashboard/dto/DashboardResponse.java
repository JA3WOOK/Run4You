package com.run4you.dashboard.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * 본사 관제 대시보드 집계 응답.
 * 5번 역할 데이터(정비 리포트·정산·진단서)에서 산출한다.
 */
public record DashboardResponse(
        SettlementSummary settlement,
        RepairSummary repair,
        GradeDistribution gradeDistribution,
        List<CategoryDefect> defectsByCategory,
        List<EquipmentMtbf> mtbf,
        Double overallMtbfDays,
        List<EngineerStat> engineerStats
) {
    /** 정산 요약 (검토대기/승인/위변조 금액·건수) */
    public record SettlementSummary(
            long totalCount,
            BigDecimal totalBilled,
            BigDecimal pendingAmount,
            BigDecimal approvedAmount,
            long pendingCount,
            long approvedCount,
            long rejectedCount,
            long flaggedCount
    ) {
    }

    /** 수리·비용 요약 */
    public record RepairSummary(
            long totalReports,
            BigDecimal totalPartsCost,
            BigDecimal totalLaborCost,
            BigDecimal totalCost
    ) {
    }

    /** 진단서 등급 분포 (A~D 개수, 평균 건강점수) */
    public record GradeDistribution(long a, long b, long c, long d, double avgScore) {
    }

    /** 부품 카테고리별 결함(교체) 통계 */
    public record CategoryDefect(String category, int replacedQuantity, int occurrences) {
    }

    /** 기자재별 MTBF (평균 고장 간격, 일). 고장 1회뿐이면 mtbfDays = null */
    public record EquipmentMtbf(Long equipmentId, int failureCount, Double mtbfDays) {
    }

    /** 엔지니어별 수리 건수 */
    public record EngineerStat(Long engineerId, long repairCount) {
    }
}
