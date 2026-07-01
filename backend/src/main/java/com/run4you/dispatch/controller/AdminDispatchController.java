package com.run4you.dispatch.controller;

import com.run4you.dispatch.dto.ActiveDispatchResponse;
import com.run4you.dispatch.service.AdminDispatchService;
import com.run4you.dispatch.support.AuthFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 통합 관제 대시보드 API.
 *
 * <ul>
 *   <li>GET /api/admin/dispatches/active — 브랜드(총괄=전체) 진행 중 출동 목록(초기 스냅샷)</li>
 * </ul>
 *
 * <p>이후 실시간 갱신은 기존 {@code GET /api/notifications/subscribe} SSE 의
 * dispatch/location 이벤트를 관제진이 그대로 수신해 처리한다(별도 스트림 불필요).
 *
 * <p>⚠ 도메인④(DispatchStatusController) 컨벤션에 맞춰 ApiResponse 래핑 없이
 * 페이로드를 그대로 반환한다 → 프론트는 res.data 를 직접 사용.
 */
@RestController
@RequestMapping("/api/admin/dispatches")
@RequiredArgsConstructor
public class AdminDispatchController {

    private final AdminDispatchService adminDispatchService;
    private final AuthFacade authFacade;

    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('BRAND_ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<ActiveDispatchResponse>> active() {
        Long adminUserId = authFacade.currentUserId();
        return ResponseEntity.ok(adminDispatchService.getActiveDispatches(adminUserId));
    }
}
