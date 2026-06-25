package com.run4you.settlement.service;

import com.run4you.report.entity.RepairReport;
import com.run4you.report.entity.RepairReportParts;
import com.run4you.report.repository.RepairReportRepository;
import com.run4you.settlement.dto.SettlementGenerateRequest;
import com.run4you.settlement.dto.SettlementListResponse;
import com.run4you.settlement.dto.SettlementResponse;
import com.run4you.settlement.entity.ApprovalStatus;
import com.run4you.settlement.entity.Settlement;
import com.run4you.settlement.entity.VerificationStatus;
import com.run4you.settlement.repository.SettlementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Year;
import java.util.List;

/**
 * 정산 핵심 로직.
 *  - 정비 리포트로부터 정산 생성: 긴급수수료(EMERGENCY 30%)·합계·VAT 계산 + 단가 검증(FLAGGED)
 *  - 정산 목록(요약 카드 + 목록), 승인/반려
 */
@Service
@RequiredArgsConstructor
public class SettlementService {

    private static final BigDecimal EMERGENCY_RATE = new BigDecimal("0.30"); // 긴급수수료율 30%
    private static final BigDecimal VAT_RATE = new BigDecimal("0.10");       // 부가세 10%

    private final SettlementRepository settlementRepository;
    private final RepairReportRepository reportRepository;

    /** 정비 리포트 1건으로 정산을 생성한다. */
    @Transactional
    public SettlementResponse generate(SettlementGenerateRequest req) {
        if (settlementRepository.existsByReportId(req.reportId())) {
            throw new IllegalArgumentException("이미 해당 리포트(" + req.reportId() + ")의 정산이 존재합니다.");
        }

        RepairReport report = reportRepository.findById(req.reportId())
                .orElseThrow(() -> new IllegalArgumentException("정비 리포트를 찾을 수 없습니다: " + req.reportId()));

        BigDecimal partsCost = nz(report.getPartsCost());
        BigDecimal laborCost = nz(report.getLaborCost());
        BigDecimal base = partsCost.add(laborCost);

        boolean emergency = "EMERGENCY".equalsIgnoreCase(req.priority());
        BigDecimal emergencyFee = emergency
                ? base.multiply(EMERGENCY_RATE).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO.setScale(2);

        BigDecimal billed = base.add(emergencyFee);
        BigDecimal vat = billed.multiply(VAT_RATE).setScale(2, RoundingMode.HALF_UP);

        // 단가 검증: 리포트 부품 중 하나라도 청구단가 != 표준단가면 FLAGGED
        List<RepairReportParts> mismatched = report.getParts().stream()
                .filter(p -> !p.isPriceMatched())
                .toList();

        VerificationStatus verification = mismatched.isEmpty()
                ? VerificationStatus.VERIFIED
                : VerificationStatus.FLAGGED;

        String flagReason = mismatched.isEmpty() ? null : mismatched.stream()
                .map(p -> String.format("%s 청구단가 %,.0f원 ≠ 표준단가 %,.0f원",
                        p.getPartCode(), p.getAppliedPrice(), p.getStandardPrice()))
                .reduce((a, b) -> a + " / " + b)
                .orElse(null);

        Settlement settlement = Settlement.builder()
                .reportId(report.getId())
                .brandId(req.brandId())
                .engineerId(report.getEngineerId())
                .invoiceNumber("AS-" + Year.now().getValue() + "-" + report.getId())
                .laborCost(laborCost)
                .partsCost(partsCost)
                .emergencyFee(emergencyFee)
                .billedAmount(billed)
                .vatAmount(vat)
                .verificationStatus(verification)
                .approvalStatus(ApprovalStatus.PENDING)
                .flagReason(flagReason)
                .build();

        return SettlementResponse.from(settlementRepository.save(settlement));
    }

    /** 정산 목록 + 요약 카드 */
    @Transactional(readOnly = true)
    public SettlementListResponse list() {
        List<Settlement> all = settlementRepository.findAllByOrderByCreatedAtDesc();

        BigDecimal reviewPending = sumBilled(settlementRepository.findByApprovalStatus(ApprovalStatus.PENDING));
        BigDecimal approved = sumBilled(settlementRepository.findByApprovalStatus(ApprovalStatus.APPROVED));
        long flaggedCount = settlementRepository.countByVerificationStatus(VerificationStatus.FLAGGED);

        List<SettlementResponse> items = all.stream().map(SettlementResponse::from).toList();
        return new SettlementListResponse(
                new SettlementListResponse.Summary(reviewPending, approved, flaggedCount),
                items);
    }

    @Transactional(readOnly = true)
    public SettlementResponse get(Long id) {
        return SettlementResponse.from(findOrThrow(id));
    }

    /** 승인 */
    @Transactional
    public SettlementResponse approve(Long id, Long approverId) {
        Settlement s = findOrThrow(id);
        s.approve(approverId);
        return SettlementResponse.from(s);
    }

    /** 반려 */
    @Transactional
    public SettlementResponse reject(Long id, Long approverId) {
        Settlement s = findOrThrow(id);
        s.reject(approverId);
        return SettlementResponse.from(s);
    }

    // --- helpers ---

    private Settlement findOrThrow(Long id) {
        return settlementRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("정산 내역을 찾을 수 없습니다: " + id));
    }

    private BigDecimal sumBilled(List<Settlement> list) {
        return list.stream()
                .map(Settlement::getBilledAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}
