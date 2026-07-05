package com.run4you.lms.controller;

import com.run4you.common.response.ApiResponse;
import com.run4you.lms.dto.TrainingStatusResponse;
import com.run4you.lms.service.AdminTrainingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 관리자 교육 이수 현황 API.
 * <ul>
 *   <li>GET /api/lms/admin/training-status — 전체 이수율 · 합격 현황 (플랫폼 총괄)</li>
 * </ul>
 *
 * 기존 {@code LmsController}(/api/lms) 를 수정하지 않도록 별도 컨트롤러로 분리했다.
 */
@RestController
@RequestMapping("/api/lms/admin")
@RequiredArgsConstructor
public class AdminTrainingController {

    private final AdminTrainingService adminTrainingService;

    @GetMapping("/training-status")
    @PreAuthorize("hasAnyRole('BRAND_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<TrainingStatusResponse>> getTrainingStatus() {
        return ResponseEntity.ok(ApiResponse.success(adminTrainingService.getTrainingStatus()));
    }
}