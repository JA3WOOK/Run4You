package com.run4you.dashboard.service;

import com.run4you.asrequest.repository.AsRequestRepository;
import com.run4you.brand.entity.Brand;
import com.run4you.brand.entity.BrandStatus;
import com.run4you.brand.repository.BrandRepository;
import com.run4you.dashboard.dto.SuperDashboardResponse;
import com.run4you.dashboard.dto.SuperDashboardResponse.*;
import com.run4you.matching.repository.AssignmentRepository;
import com.run4you.settlement.entity.Settlement;
import com.run4you.settlement.repository.SettlementRepository;
import com.run4you.user.entity.Role;
import com.run4you.user.entity.User;
import com.run4you.user.entity.UserStatus;
import com.run4you.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SuperDashboardService {

    private final AsRequestRepository asRequestRepository;
    private final BrandRepository brandRepository;
    private final UserRepository userRepository;
    private final SettlementRepository settlementRepository;
    private final AssignmentRepository assignmentRepository;

    @Transactional(readOnly = true)
    public SuperDashboardResponse getSuperDashboard() {
        long totalAs = asRequestRepository.count();
        long activeEngineers = userRepository.countByRoleAndStatus(Role.ENGINEER, UserStatus.ACTIVE);

        List<Brand> pendingBrands = brandRepository.findAllByStatus(BrandStatus.PENDING);
        List<User> pendingUsers = userRepository.findAllByStatus(UserStatus.PENDING);
        int pendingApprovals = pendingBrands.size() + pendingUsers.size();

        // 이번 달 청구액
        List<Settlement> settlements = settlementRepository.findAll();
        YearMonth thisMonth = YearMonth.now();
        BigDecimal thisMonthCommission = settlements.stream()
                .filter(s -> s.getCreatedAt() != null
                        && YearMonth.from(s.getCreatedAt()).equals(thisMonth))
                .map(Settlement::getBilledAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 브랜드별 A/S 건수 (실제 접수 기준)
        Map<Long, Long> asCountByBrand = asRequestRepository.countGroupByBrandId().stream()
                .collect(Collectors.toMap(r -> (Long) r[0], r -> (Long) r[1]));
        Map<Long, Long> completedCountByBrand = asRequestRepository.countCompletedGroupByBrandId().stream()
                .collect(Collectors.toMap(r -> (Long) r[0], r -> (Long) r[1]));

        Map<Long, List<Settlement>> settlementsByBrand = settlements.stream()
                .filter(s -> s.getBrandId() != null)
                .collect(Collectors.groupingBy(Settlement::getBrandId));

        // 브랜드별 평균 처리시간 (접수→완료, 단위: 시간)
        Map<Long, Double> avgProcessingHoursByBrand = assignmentRepository.findCompletedTimesWithBrand().stream()
                .collect(Collectors.groupingBy(
                        r -> (Long) r[0],
                        Collectors.averagingDouble(r -> {
                            LocalDateTime requested = (LocalDateTime) r[1];
                            LocalDateTime completed = (LocalDateTime) r[2];
                            return Duration.between(requested, completed).toMinutes() / 60.0;
                        })
                ));

        List<BrandStat> brandStats = brandRepository.findAll().stream()
                .map(b -> {
                    List<Settlement> bs = settlementsByBrand.getOrDefault(b.getId(), List.of());
                    BigDecimal total = bs.stream().map(Settlement::getBilledAmount)
                            .filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
                    long asCount = asCountByBrand.getOrDefault(b.getId(), 0L);
                    long completedCount = completedCountByBrand.getOrDefault(b.getId(), 0L);
                    return new BrandStat(b.getId(), b.getName(), b.getStatus().name(),
                            b.getCommissionRate(), asCount, completedCount, total,
                            avgProcessingHoursByBrand.getOrDefault(b.getId(), null));
                })
                .toList();

        // 카테고리별 통계
        List<CategoryStat> categoryStats = asRequestRepository.countGroupByFaultCategory().stream()
                .map(r -> new CategoryStat((String) r[0], (Long) r[1]))
                .sorted(Comparator.comparingLong(CategoryStat::count).reversed())
                .toList();

        // 월별 수수료 (최근 6개월)
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM");
        List<MonthlyCommission> monthlyCommission = new ArrayList<>();
        for (int i = 5; i >= 0; i--) {
            YearMonth ym = thisMonth.minusMonths(i);
            BigDecimal amount = settlements.stream()
                    .filter(s -> s.getCreatedAt() != null && YearMonth.from(s.getCreatedAt()).equals(ym))
                    .map(Settlement::getBilledAmount)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            monthlyCommission.add(new MonthlyCommission(ym.format(fmt), amount));
        }

        List<PendingBrandItem> pendingBrandItems = pendingBrands.stream()
                .map(b -> new PendingBrandItem(b.getId(), b.getName(), b.getBusinessNo(), b.getCommissionRate()))
                .toList();
        List<PendingUserItem> pendingUserItems = pendingUsers.stream()
                .map(u -> new PendingUserItem(u.getId(), u.getName(), u.getEmail(), u.getRole().name()))
                .toList();

        return new SuperDashboardResponse(
                new KpiSummary(totalAs, pendingApprovals, activeEngineers, thisMonthCommission),
                pendingBrandItems,
                pendingUserItems,
                brandStats,
                categoryStats,
                monthlyCommission
        );
    }
}
