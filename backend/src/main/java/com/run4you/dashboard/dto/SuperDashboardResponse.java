package com.run4you.dashboard.dto;

import java.math.BigDecimal;
import java.util.List;

public record SuperDashboardResponse(
        KpiSummary kpi,
        List<PendingBrandItem> pendingBrands,
        List<PendingUserItem> pendingUsers,
        List<BrandStat> brandStats,
        List<CategoryStat> categoryStats,
        List<MonthlyCommission> monthlyCommission
) {
    public record KpiSummary(
            long totalAsRequests,
            int pendingApprovals,
            long activeEngineers,
            BigDecimal thisMonthCommission
    ) {}

    public record PendingBrandItem(
            Long id,
            String name,
            String businessNo,
            BigDecimal commissionRate
    ) {}

    public record PendingUserItem(
            Long id,
            String name,
            String email,
            String role
    ) {}

    public record BrandStat(
            Long id,
            String name,
            String status,
            BigDecimal commissionRate,
            long totalAsCount,
            long completedAsCount,
            BigDecimal totalBilled,
            Double avgProcessingHours
    ) {}

    public record CategoryStat(String category, long count) {}

    public record MonthlyCommission(String month, BigDecimal amount) {}
}