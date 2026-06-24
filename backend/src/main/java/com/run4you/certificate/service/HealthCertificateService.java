package com.run4you.certificate.service;

import com.run4you.certificate.dto.CertificateIssueRequest;
import com.run4you.certificate.dto.CertificateResponse;
import com.run4you.certificate.entity.HealthCertificate;
import com.run4you.certificate.entity.HealthGrade;
import com.run4you.certificate.repository.HealthCertificateRepository;
import com.run4you.report.entity.RepairReportParts;
import com.run4you.report.repository.RepairReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Year;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * 건강 진단서 §20 등급 산정.
 *  건강 점수 = 100 − (수리횟수 + 사용기간 + 교체부품 + 최근고장) 감점 합   (하한 0)
 *  - 수리 횟수, 교체 부품 수: 내 리포트 테이블에서 기자재별 집계
 *  - 사용 기간, 최근 고장: 요청값(구매일·최근고장여부)으로 계산
 */
@Service
@RequiredArgsConstructor
public class HealthCertificateService {

    private final HealthCertificateRepository certificateRepository;
    private final RepairReportRepository reportRepository;

    @Transactional
    public CertificateResponse issue(CertificateIssueRequest req) {
        long repairCount = reportRepository.countByEquipmentId(req.equipmentId());

        int replacedPartsCount = reportRepository.findByEquipmentId(req.equipmentId()).stream()
                .flatMap(r -> r.getParts().stream())
                .mapToInt(RepairReportParts::getQuantity)
                .sum();

        double usageYears = ChronoUnit.DAYS.between(req.purchasedAt(), LocalDate.now()) / 365.0;

        int deduction = repairDeduction(repairCount)
                + usageDeduction(usageYears)
                + partsDeduction(replacedPartsCount)
                + (req.recentFault() ? 20 : 0);

        int score = Math.max(0, 100 - deduction);
        HealthGrade grade = HealthGrade.fromScore(score);

        String certificateNo = "HC-" + Year.now().getValue() + "-" + req.equipmentId()
                + "-" + (System.currentTimeMillis() % 100000);

        HealthCertificate cert = HealthCertificate.builder()
                .equipmentId(req.equipmentId())
                .reportId(req.reportId())
                .certificateNo(certificateNo)
                .healthScore(score)
                .grade(grade)
                .repairCount((int) repairCount)
                .usageYears((int) Math.floor(usageYears))
                .replacedPartsCount(replacedPartsCount)
                .recentFault(req.recentFault())
                .build();

        return CertificateResponse.from(certificateRepository.save(cert));
    }

    @Transactional(readOnly = true)
    public List<CertificateResponse> list() {
        return certificateRepository.findAllByOrderByIssuedAtDesc().stream()
                .map(CertificateResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public CertificateResponse get(Long id) {
        return CertificateResponse.from(certificateRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("진단서를 찾을 수 없습니다: " + id)));
    }

    @Transactional(readOnly = true)
    public List<CertificateResponse> byEquipment(Long equipmentId) {
        return certificateRepository.findByEquipmentIdOrderByIssuedAtDesc(equipmentId).stream()
                .map(CertificateResponse::from).toList();
    }

    // --- §20 감점 기준 ---

    /** 수리 횟수: 0회 0 / 1~2회 −10 / 3~5회 −20 / 6회+ −30 */
    private int repairDeduction(long count) {
        if (count == 0) return 0;
        if (count <= 2) return 10;
        if (count <= 5) return 20;
        return 30;
    }

    /** 사용 기간: 1년 미만 0 / 1~3년 −10 / 3~5년 −20 / 5년+ −30 */
    private int usageDeduction(double years) {
        if (years < 1) return 0;
        if (years < 3) return 10;
        if (years < 5) return 20;
        return 30;
    }

    /** 교체 부품 수: 0~2개 0 / 3~5개 −10 / 6개+ −20 */
    private int partsDeduction(int count) {
        if (count <= 2) return 0;
        if (count <= 5) return 10;
        return 20;
    }
}
