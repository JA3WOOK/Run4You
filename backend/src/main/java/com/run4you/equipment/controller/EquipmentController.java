package com.run4you.equipment.controller;

import com.run4you.asrequest.dto.AsRequestHistoryDto;
import com.run4you.equipment.dto.EquipmentCreateDto;
import com.run4you.equipment.dto.EquipmentListResponseDto;
import com.run4you.equipment.dto.EquipmentResponseDto;
import com.run4you.equipment.dto.EquipmentSearchDto;
import com.run4you.equipment.service.EquipmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.run4you.common.response.ApiResponse;

@RestController
@RequestMapping("/api/equipment")
@RequiredArgsConstructor
public class EquipmentController {

    private final EquipmentService equipmentService;

    // 1. 기자재 목록 조회
    @GetMapping
    public ResponseEntity<ApiResponse<EquipmentListResponseDto>> getEquipmentList(
            @ModelAttribute EquipmentSearchDto searchDto) {
        return ResponseEntity.ok(ApiResponse.success(equipmentService.getEquipmentList(searchDto)));
    }

    // 2. 기자재 등록
    @PostMapping
    public ResponseEntity<ApiResponse<EquipmentResponseDto>> registerEquipment(
            @RequestBody @Valid EquipmentCreateDto createDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of(equipmentService.registerEquipment(createDto), "success"));
    }

    // 3. 이력보기 모달
    @GetMapping("/{equipmentId}/history")
    public ResponseEntity<ApiResponse<AsRequestHistoryDto>> getRepairHistory(
            @PathVariable Long equipmentId){
        return ResponseEntity.ok(
                ApiResponse.success(equipmentService.getRepairHistory(equipmentId)));
    }
}
