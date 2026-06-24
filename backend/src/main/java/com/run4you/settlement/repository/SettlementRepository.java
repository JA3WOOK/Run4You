package com.run4you.settlement.repository;

import com.run4you.settlement.entity.ApprovalStatus;
import com.run4you.settlement.entity.Settlement;
import com.run4you.settlement.entity.VerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {

    boolean existsByReportId(Long reportId);

    List<Settlement> findAllByOrderByCreatedAtDesc();

    long countByVerificationStatus(VerificationStatus status);

    List<Settlement> findByApprovalStatus(ApprovalStatus status);

    // 팀원(asrequest) 영수증 상세 - 리포트로 정산 조회
    Optional<Settlement> findByReportId(Long reportId);
}
