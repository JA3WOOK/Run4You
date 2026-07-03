package com.run4you.matching.controller;

import com.run4you.common.response.ApiResponse;
import com.run4you.matching.dto.*;
import com.run4you.matching.entity.Assignment;
import com.run4you.matching.service.MatchingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assignments")
@RequiredArgsConstructor
public class MatchingController {

    private final MatchingService matchingService;

    /**
      출동 요청 대기열 조회
      반경 내 AS 요청을 이 엔지니어의 개인 스코어 순으로 반환.
      GET /api/assignments/queue
     */
    @GetMapping("/queue")
    @PreAuthorize("hasRole('ENGINEER')")
    public ResponseEntity<ApiResponse<List<MatchingQueueItemResponse>>> getMatchingQueue(
            @AuthenticationPrincipal String email
    ) {
        List<MatchingQueueItemResponse> queue = matchingService.getMatchingQueue(email);
        return ResponseEntity.ok(ApiResponse.success(queue));
    }

    /**
     로그인 엔지니어의 현재 진행 중인 배정 조회 (로그아웃 후 재접속 시 화면 복구용)
     GET /api/assignments/my-active
     */
    @GetMapping("/my-active")
    @PreAuthorize("hasRole('ENGINEER')")
    public ResponseEntity<ApiResponse<ActiveAssignmentResponse>> getMyActiveAssignment(
            @AuthenticationPrincipal String email
    ) {
        ActiveAssignmentResponse active = matchingService.getMyActiveAssignment(email);
        return ResponseEntity.ok(ApiResponse.success(active));
    }

    /**
      특정 AS 요청 상세 조회 (수락 전 상세 화면)
      고장 기자재 정보 + 이 엔지니어의 가중치 배정 점수 반환
      GET /api/assignments/requests/{asRequestId}/detail
     */
    @GetMapping("/requests/{asRequestId}/detail")
    @PreAuthorize("hasRole('ENGINEER')")
    public ResponseEntity<ApiResponse<AssignmentDetailResponse>> getRequestDetail(
            @PathVariable Long asRequestId,
            @AuthenticationPrincipal String email
    ) {
        AssignmentDetailResponse detail = matchingService.getRequestDetail(asRequestId, email);
        return ResponseEntity.ok(ApiResponse.success(detail));
    }

    /**
      출동 요청 수락 — Redisson 분산 락 적용
      동시에 여러 엔지니어가 수락을 시도해도 단 1명만 배정 확정.
      POST /api/assignments/requests/{asRequestId}/accept
     */
    @PostMapping("/requests/{asRequestId}/accept")
    @PreAuthorize("hasRole('ENGINEER')")
    public ResponseEntity<ApiResponse<ActiveAssignmentResponse>> acceptAssignment(
            @PathVariable Long asRequestId,
            @AuthenticationPrincipal String email
    ) {
        Assignment assignment = matchingService.acceptAssignment(asRequestId, email);
        return ResponseEntity.ok(ApiResponse.success(ActiveAssignmentResponse.of(assignment)));
    }

    /**
      후보 스코어 로그 조회 — 배정 근거 확인용
      GET /api/assignments/{asRequestId}/candidates
     */
    @GetMapping("/{asRequestId}/candidates")
    @PreAuthorize("hasAnyRole('BRAND_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<CandidateScoreResponse>>> getCandidateScores(
            @PathVariable Long asRequestId
    ) {
        List<CandidateScoreResponse> candidates = matchingService.getCandidateScores(asRequestId);
        return ResponseEntity.ok(ApiResponse.success(candidates));
    }

    /** 리포트 미작성 수리 완료 건 조회 */
    @GetMapping("/pending-reports")
    @PreAuthorize("hasRole('ENGINEER')")
    public ResponseEntity<ApiResponse<List<PendingReportResponse>>> getPendingReports(
            @AuthenticationPrincipal String email
    ) {
        return ResponseEntity.ok(ApiResponse.success(matchingService.getPendingReports(email)));
    }
}