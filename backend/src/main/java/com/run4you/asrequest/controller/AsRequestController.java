package com.run4you.asrequest.controller;

import com.run4you.asrequest.dto.*;
import com.run4you.asrequest.service.AsRequestService;
import com.run4you.equipment.entity.EquipmentCategory;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.run4you.common.response.ApiResponse;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/as-requests")
@RequiredArgsConstructor
public class AsRequestController {

    private final AsRequestService asRequestService;

    // A/S 접수 생성
    @PostMapping
    public ResponseEntity<ApiResponse<AsRequestResponseDto>> createAsRequest(
            @RequestBody @Valid AsRequestCreateDto createDto){

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of(asRequestService.createAsRequest(createDto), "success"));
    }

    //  진단서 및 영수증 목록 조회
    @GetMapping("/receipts")
    public ResponseEntity<ApiResponse<ReceiptListResponseDto>> getReceipts(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)LocalDate startDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)LocalDate endDate,

            @RequestParam(required = false)EquipmentCategory category
            ) {
        ReceiptSearchDto searchDto = ReceiptSearchDto.builder()
                .startDate(startDate)
                .endDate(endDate)
                .category(category)
                .build();

        return ResponseEntity.ok(ApiResponse.success(asRequestService.getReceipts(searchDto)));
    }

    @GetMapping("/receipts/{asRequestId}")
    public ResponseEntity<ApiResponse<ReceiptDetailResponseDto>> getReceiptDetail(
            @PathVariable Long asRequestId) {

        return ResponseEntity.ok(
                ApiResponse.success(asRequestService.getReceiptDetail(asRequestId)));
    }
}
