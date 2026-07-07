package com.run4you.dashboard.service;

import com.run4you.certificate.entity.HealthCertificate;
import com.run4you.certificate.entity.HealthGrade;
import com.run4you.certificate.repository.HealthCertificateRepository;
import com.run4you.dashboard.dto.DashboardResponse;
import com.run4you.dashboard.dto.DashboardResponse.*;
import com.run4you.equipment.entity.Equipment;
import com.run4you.equipment.repository.EquipmentRepository;
import com.run4you.part.entity.Parts;
import com.run4you.part.repository.PartsRepository;
import com.run4you.report.entity.RepairReport;
import com.run4you.report.entity.RepairReportParts;
import com.run4you.report.repository.RepairReportRepository;
import com.run4you.settlement.entity.ApprovalStatus;
import com.run4you.settlement.entity.Settlement;
import com.run4you.settlement.entity.VerificationStatus;
import com.run4you.settlement.repository.SettlementRepository;
import com.run4you.user.entity.User;
import com.run4you.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 관제 대시보드 집계 — 정산/리포트/진단서 데이터에서 지표를 산출한다.
 */
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final SettlementRepository settlementRepository;
    private final RepairReportRepository reportRepository;
    private final HealthCertificateRepository certificateRepository;
    private final PartsRepository partsRepository;
    private final UserRepository userRepository;
    private final EquipmentRepository equipmentRepository;

    @Transactional(readOnly = true)
    public DashboardResponse getDashboard() {
        List<Settlement> settlements = settlementRepository.findAll();
        List<RepairReport> reports = reportRepository.findAll();
        List<HealthCertificate> certs = certificateRepository.findAll();
        List<Parts> parts = partsRepository.findAll();

        Map<Long, String> equipmentNames = equipmentNames(reports);
        Map<Long, String> engineerNames = engineerNames(reports);

        List<EquipmentMtbf> mtbfList = mtbfByEquipment(reports, equipmentNames);

        return new DashboardResponse(
                settlementSummary(settlements),
                repairSummary(reports),
                gradeDistribution(certs),
                defectsByCategory(reports, parts),
                mtbfList,
                overallMtbf(mtbfList),
                engineerStats(reports, engineerNames),
                topFailingEquipment(mtbfList)
        );
    }

    /** 정비 리포트에 등장하는 기자재 ID → 기기명(별칭, 없으면 모델명) 매핑 */
    private Map<Long, String> equipmentNames(List<RepairReport> reports) {
        List<Long> ids = reports.stream()
                .map(RepairReport::getEquipmentId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (ids.isEmpty()) return Map.of();
        return equipmentRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(
                        Equipment::getId,
                        e -> (e.getName() != null && !e.getName().isBlank())
                                ? e.getName()
                                : (e.getModelName() != null ? e.getModelName() : "기자재 #" + e.getId())
                ));
    }

    /** 정비 리포트에 등장하는 엔지니어 ID → 이름 매핑 */
    private Map<Long, String> engineerNames(List<RepairReport> reports) {
        List<Long> ids = reports.stream()
                .map(RepairReport::getEngineerId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (ids.isEmpty()) return Map.of();
        return userRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(User::getId, User::getName));
    }

    private SettlementSummary settlementSummary(List<Settlement> list) {
        BigDecimal total = sum(list.stream().map(Settlement::getBilledAmount));
        BigDecimal pending = sum(list.stream()
                .filter(s -> s.getApprovalStatus() == ApprovalStatus.PENDING)
                .map(Settlement::getBilledAmount));
        BigDecimal approved = sum(list.stream()
                .filter(s -> s.getApprovalStatus() == ApprovalStatus.APPROVED)
                .map(Settlement::getBilledAmount));
        long pendingCount = list.stream().filter(s -> s.getApprovalStatus() == ApprovalStatus.PENDING).count();
        long approvedCount = list.stream().filter(s -> s.getApprovalStatus() == ApprovalStatus.APPROVED).count();
        long rejectedCount = list.stream().filter(s -> s.getApprovalStatus() == ApprovalStatus.REJECTED).count();
        long flaggedCount = list.stream().filter(s -> s.getVerificationStatus() == VerificationStatus.FLAGGED).count();
        return new SettlementSummary(list.size(), total, pending, approved,
                pendingCount, approvedCount, rejectedCount, flaggedCount);
    }

    private RepairSummary repairSummary(List<RepairReport> reports) {
        BigDecimal partsCost = sum(reports.stream().map(RepairReport::getPartsCost));
        BigDecimal laborCost = sum(reports.stream().map(RepairReport::getLaborCost));
        BigDecimal totalCost = sum(reports.stream().map(RepairReport::getTotalCost));
        return new RepairSummary(reports.size(), partsCost, laborCost, totalCost);
    }

    private GradeDistribution gradeDistribution(List<HealthCertificate> certs) {
        long a = certs.stream().filter(c -> c.getGrade() == HealthGrade.A).count();
        long b = certs.stream().filter(c -> c.getGrade() == HealthGrade.B).count();
        long c = certs.stream().filter(x -> x.getGrade() == HealthGrade.C).count();
        long d = certs.stream().filter(x -> x.getGrade() == HealthGrade.D).count();
        double avg = certs.isEmpty() ? 0.0
                : certs.stream().mapToInt(HealthCertificate::getHealthScore).average().orElse(0.0);
        return new GradeDistribution(a, b, c, d, Math.round(avg * 10) / 10.0);
    }

    private List<CategoryDefect> defectsByCategory(List<RepairReport> reports, List<Parts> parts) {
        Map<String, String> codeToCategory = parts.stream()
                .collect(Collectors.toMap(Parts::getPartCode,
                        p -> p.getCategory() == null ? "기타" : p.getCategory(), (x, y) -> x));

        Map<String, int[]> agg = new LinkedHashMap<>(); // category -> [수량, 발생건수]
        for (RepairReport r : reports) {
            for (RepairReportParts p : r.getParts()) {
                String cat = codeToCategory.getOrDefault(p.getPartCode(), "기타");
                int[] v = agg.computeIfAbsent(cat, k -> new int[2]);
                v[0] += p.getQuantity();
                v[1] += 1;
            }
        }
        return agg.entrySet().stream()
                .map(e -> new CategoryDefect(e.getKey(), e.getValue()[0], e.getValue()[1]))
                .sorted(Comparator.comparingInt(CategoryDefect::replacedQuantity).reversed())
                .toList();
    }

    private List<EquipmentMtbf> mtbfByEquipment(List<RepairReport> reports, Map<Long, String> equipmentNames) {
        Map<Long, List<RepairReport>> byEquip = reports.stream()
                .filter(r -> r.getEquipmentId() != null)
                .collect(Collectors.groupingBy(RepairReport::getEquipmentId));

        List<EquipmentMtbf> result = new ArrayList<>();
        for (Map.Entry<Long, List<RepairReport>> e : byEquip.entrySet()) {
            List<RepairReport> sorted = e.getValue().stream()
                    .filter(r -> r.getCreatedAt() != null)
                    .sorted(Comparator.comparing(RepairReport::getCreatedAt))
                    .toList();
            Double mtbf = null;
            if (sorted.size() >= 2) {
                long totalDays = 0;
                for (int i = 1; i < sorted.size(); i++) {
                    totalDays += ChronoUnit.DAYS.between(
                            sorted.get(i - 1).getCreatedAt(), sorted.get(i).getCreatedAt());
                }
                mtbf = Math.round((double) totalDays / (sorted.size() - 1) * 10) / 10.0;
            }
            String name = equipmentNames.getOrDefault(e.getKey(), "기자재 #" + e.getKey());
            result.add(new EquipmentMtbf(e.getKey(), name, sorted.size(), mtbf));
        }
        result.sort(Comparator.comparing(EquipmentMtbf::equipmentId));
        return result;
    }

    private Double overallMtbf(List<EquipmentMtbf> mtbfList) {
        List<Double> values = mtbfList.stream()
                .map(EquipmentMtbf::mtbfDays)
                .filter(Objects::nonNull)
                .toList();
        if (values.isEmpty()) return null;
        double avg = values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        return Math.round(avg * 10) / 10.0;
    }

    private List<EngineerStat> engineerStats(List<RepairReport> reports, Map<Long, String> engineerNames) {
        Map<Long, Long> counts = reports.stream()
                .filter(r -> r.getEngineerId() != null)
                .collect(Collectors.groupingBy(RepairReport::getEngineerId, Collectors.counting()));
        return counts.entrySet().stream()
                .map(e -> new EngineerStat(
                        e.getKey(),
                        engineerNames.getOrDefault(e.getKey(), "엔지니어 #" + e.getKey()),
                        e.getValue()))
                .sorted(Comparator.comparingLong(EngineerStat::repairCount).reversed())
                .toList();
    }

    private List<EquipmentFailure> topFailingEquipment(List<EquipmentMtbf> mtbfList) {
        return mtbfList.stream()
                .sorted(Comparator.comparingInt(EquipmentMtbf::failureCount).reversed())
                .limit(5)
                .map(m -> new EquipmentFailure(m.equipmentId(), m.equipmentName(), m.failureCount()))
                .toList();
    }

    private BigDecimal sum(java.util.stream.Stream<BigDecimal> stream) {
        return stream.filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}