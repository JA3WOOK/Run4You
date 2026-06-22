package com.run4you.matching.repository;

import com.run4you.matching.entity.AssignmentCandidate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AssignmentCandidateRepository extends JpaRepository<AssignmentCandidate, Long> {

    /**
      특정 AS 요청의 후보 스코어 목록 (점수 내림차순)
      BRAND_ADMIN 관제 화면: 배정 근거 추적용
     */
    @Query("""
            SELECT ac FROM AssignmentCandidate ac
            JOIN FETCH ac.engineer
            WHERE ac.asRequest.id = :asRequestId
            ORDER BY ac.totalScore DESC
            """)
    List<AssignmentCandidate> findByAsRequestIdOrderByTotalScoreDesc(@Param("asRequestId") Long asRequestId);
}
