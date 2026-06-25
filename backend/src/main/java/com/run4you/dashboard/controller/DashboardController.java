package com.run4you.dashboard.controller;

import com.run4you.common.response.ApiResponse;
import com.run4you.dashboard.dto.DashboardResponse;
import com.run4you.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 본사 관제 대시보드 API.
 *  GET /api/dashboard  정산·수리·진단서 등급·결함통계·MTBF·엔지니어 통계 일괄 집계
 */
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public ResponseEntity<ApiResponse<DashboardResponse>> dashboard() {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getDashboard()));
    }
}
