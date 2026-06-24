package com.run4you.report.service;

import com.run4you.part.entity.Parts;
import com.run4you.part.service.PartService;
import com.run4you.report.dto.PartLineRequest;
import com.run4you.report.dto.ReportCreateRequest;
import com.run4you.report.dto.ReportResponse;
import com.run4you.report.entity.RepairReport;
import com.run4you.report.entity.RepairReportParts;
import com.run4you.report.repository.RepairReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 정비 리포트 핵심 로직.
 *  1) 교체 부품을 부품 마스터와 대조 검증(존재 여부 + 청구단가 vs 표준단가)
 *  2) 부품비·총비용 합산
 *  3) 리포트 저장
 */
@Service
@RequiredArgsConstructor
public class ReportService {

    private final RepairReportRepository reportRepository;
    private final PartService partService;

    @Transactional
    public ReportResponse createReport(ReportCreateRequest req) {
        if (reportRepository.existsByAssignmentId(req.assignmentId())) {
            throw new IllegalArgumentException(
                    "이미 해당 배정(" + req.assignmentId() + ")의 정비 리포트가 존재합니다.");
        }

        RepairReport report = RepairReport.builder()
                .assignmentId(req.assignmentId())
                .asRequestId(req.asRequestId())
                .engineerId(req.engineerId())
                .equipmentId(req.equipmentId())
                .laborCost(req.laborCost())
                .diagnosis(req.diagnosis())
                .build();

        if (req.parts() != null) {
            for (PartLineRequest line : req.parts()) {
                Parts master = partService.getByCode(line.partCode());   // 없으면 예외
                boolean matched = master.getUnitPrice().compareTo(line.appliedPrice()) == 0;

                report.addPart(RepairReportParts.builder()
                        .partId(master.getId())
                        .partCode(master.getPartCode())
                        .partName(master.getName())
                        .quantity(line.quantity())
                        .appliedPrice(line.appliedPrice())
                        .standardPrice(master.getUnitPrice())
                        .priceMatched(matched)
                        .build());
            }
        }

        report.recalculateCosts();
        RepairReport saved = reportRepository.save(report);
        return ReportResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public ReportResponse getReport(Long id) {
        RepairReport report = reportRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("정비 리포트를 찾을 수 없습니다: " + id));
        return ReportResponse.from(report);
    }

    @Transactional(readOnly = true)
    public List<ReportResponse> listReports(Long engineerId) {
        List<RepairReport> reports = (engineerId == null)
                ? reportRepository.findAllByOrderByCreatedAtDesc()
                : reportRepository.findByEngineerIdOrderByCreatedAtDesc(engineerId);
        return reports.stream().map(ReportResponse::from).toList();
    }
}
