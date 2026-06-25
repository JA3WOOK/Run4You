package com.run4you.dispatch.controller;

import com.run4you.dispatch.dto.DispatchEventPayload;
import com.run4you.dispatch.dto.DispatchStatusUpdateRequest;
import com.run4you.dispatch.dto.DispatchTrackingResponse;
import com.run4you.dispatch.dto.EngineerLocationPing;
import com.run4you.dispatch.service.DispatchStatusService;
import com.run4you.dispatch.support.AuthFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 출동 관제 API.
 * <ul>
 *   <li>PATCH /api/assignments/{id}/status — 엔지니어 출동 상태 변경 (기획안 §11.4)</li>
 *   <li>GET   /api/assignments/{id}/tracking — 점주 실시간 위치/ETA/타임라인</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/assignments")
@RequiredArgsConstructor
public class DispatchStatusController {

    private final DispatchStatusService dispatchStatusService;
    private final AuthFacade authFacade;

    @PreAuthorize("hasRole('ENGINEER')")
    @PatchMapping("/{assignmentId}/status")
    public ResponseEntity<DispatchEventPayload> updateStatus(
            @PathVariable Long assignmentId,
            @Valid @RequestBody DispatchStatusUpdateRequest request) {

        Long engineerId = authFacade.currentUserId();
        return ResponseEntity.ok(
                dispatchStatusService.updateStatus(assignmentId, engineerId, request));
    }

    @PreAuthorize("hasRole('ENGINEER')")
    @PostMapping("/{assignmentId}/location")
    public ResponseEntity<DispatchEventPayload> updateLocation(
            @PathVariable Long assignmentId,
            @Valid @RequestBody EngineerLocationPing ping) {

        Long engineerId = authFacade.currentUserId();
        return ResponseEntity.ok(
                dispatchStatusService.updateEngineerLocation(assignmentId, engineerId, ping));
    }

    @PreAuthorize("hasRole('STORE_OWNER')")
    @GetMapping("/{assignmentId}/tracking")
    public ResponseEntity<DispatchTrackingResponse> tracking(@PathVariable Long assignmentId) {
        Long requesterId = authFacade.currentUserId();
        return ResponseEntity.ok(
                dispatchStatusService.getTracking(assignmentId, requesterId));
    }
}
