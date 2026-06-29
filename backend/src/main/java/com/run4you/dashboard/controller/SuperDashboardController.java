package com.run4you.dashboard.controller;

import com.run4you.common.response.ApiResponse;
import com.run4you.dashboard.dto.SuperDashboardResponse;
import com.run4you.dashboard.service.SuperDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard/super")
@RequiredArgsConstructor
public class SuperDashboardController {

    private final SuperDashboardService superDashboardService;

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<SuperDashboardResponse>> superDashboard() {
        return ResponseEntity.ok(ApiResponse.success(superDashboardService.getSuperDashboard()));
    }
}
