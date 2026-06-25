package com.run4you.settlement.controller;

import com.run4you.common.response.ApiResponse;
import com.run4you.settlement.dto.SettlementGenerateRequest;
import com.run4you.settlement.dto.SettlementListResponse;
import com.run4you.settlement.dto.SettlementResponse;
import com.run4you.settlement.service.SettlementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 정산 API (피그마 정산 관리 화면 백엔드).
 *  GET   /api/settlements              목록 + 요약 카드(검토대기/승인완료/위변조의심)
 *  GET   /api/settlements/{id}         상세
 *  POST  /api/settlements/generate     정비 리포트로부터 정산 생성
 *  PATCH /api/settlements/{id}/approve 승인
 *  PATCH /api/settlements/{id}/reject  반려
 */
@RestController
@RequestMapping("/api/settlements")
@RequiredArgsConstructor
public class SettlementController {

    private final SettlementService settlementService;

    @GetMapping
    public ResponseEntity<ApiResponse<SettlementListResponse>> list() {
        return ResponseEntity.ok(ApiResponse.success(settlementService.list()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SettlementResponse>> detail(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(settlementService.get(id)));
    }

    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<SettlementResponse>> generate(
            @Valid @RequestBody SettlementGenerateRequest request) {
        SettlementResponse res = settlementService.generate(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of(res, "정산이 생성되었습니다."));
    }

    // 승인자(approverId)는 통합 시 로그인 사용자에서 주입. 로컬에서는 쿼리 파라미터로 전달.
    @PatchMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<SettlementResponse>> approve(
            @PathVariable Long id,
            @RequestParam(required = false) Long approverId) {
        return ResponseEntity.ok(ApiResponse.success(settlementService.approve(id, approverId), "승인 완료"));
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<SettlementResponse>> reject(
            @PathVariable Long id,
            @RequestParam(required = false) Long approverId) {
        return ResponseEntity.ok(ApiResponse.success(settlementService.reject(id, approverId), "반려 완료"));
    }
}
