package com.run4you.equipment.controller;

import com.run4you.common.response.ApiResponse;
import com.run4you.equipment.dto.AdminEquipmentResponseDto;
import com.run4you.equipment.entity.EquipmentStatus;
import com.run4you.equipment.service.EquipmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/equipment")
@RequiredArgsConstructor
public class AdminEquipmentController {

    private final EquipmentService equipmentService;

    // 관리자용 - 전체 매장 기자재 목록 조회 (상태 필터 + 키워드 검색)
    @GetMapping
    @PreAuthorize("hasRole('BRAND_ADMIN')")
    public ResponseEntity<ApiResponse<List<AdminEquipmentResponseDto>>> getAdminEquipmentList(
            @RequestParam(required = false) EquipmentStatus status,
            @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(
                ApiResponse.success(equipmentService.getAdminEquipmentList(status, keyword)));
    }
}