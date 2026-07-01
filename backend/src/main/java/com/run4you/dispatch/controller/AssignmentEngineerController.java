package com.run4you.dispatch.controller;

import com.run4you.dispatch.dto.AssignmentEngineerResponse;
import com.run4you.dispatch.port.EngineerInfoPort;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 배정 엔지니어 정보 API.
 * <ul>
 *   <li>GET /api/assignments/{id}/engineer — 담당 엔지니어 이름/전화/평점/등급 (점주 추적 카드용)</li>
 * </ul>
 *
 * <p>⚠ 도메인④ 컨벤션에 맞춰 ApiResponse 래핑 없이 반환 → 프론트 res.data 직접.
 */
@RestController
@RequestMapping("/api/assignments")
@RequiredArgsConstructor
public class AssignmentEngineerController {

    private final EngineerInfoPort engineerInfoPort;

    @GetMapping("/{assignmentId}/engineer")
    @PreAuthorize("hasAnyRole('STORE_OWNER', 'BRAND_ADMIN', 'SUPER_ADMIN', 'ENGINEER')")
    public ResponseEntity<AssignmentEngineerResponse> engineer(@PathVariable Long assignmentId) {
        return ResponseEntity.ok(engineerInfoPort.findByAssignment(assignmentId));
    }
}
