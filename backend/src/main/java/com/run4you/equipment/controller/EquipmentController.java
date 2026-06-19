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

@RestController
@RequestMapping("/api/equipment")
@RequiredArgsConstructor
public class EquipmentController {

    private final EquipmentService equipmentService;

    // 1. 기자재 목록 조회
    @GetMapping
    public ResponseEntity<EquipmentListResponseDto> getEquipmentList(
            @RequestParam Long storeId,
            @ModelAttribute EquipmentSearchDto searchDto) {
        return ResponseEntity.ok(equipmentService.getEquipmentList(storeId, searchDto));
    }

    // 2. 기자재 등록
    @PostMapping
    public ResponseEntity<EquipmentResponseDto> registerEquipment(
            @RequestParam Long storeId,
            @RequestBody @Valid EquipmentCreateDto createDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(equipmentService.registerEquipment(storeId, createDto));
    }

    // 3. 이력보기 모달
    @GetMapping("/{equipmentId}/history")
    public ResponseEntity<AsRequestHistoryDto> getRepairHistory(
            @PathVariable Long equipmentId){
        return ResponseEntity.ok(equipmentService.getRepairHistory(equipmentId));
    }

}
