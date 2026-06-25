package com.run4you.report.controller;

import com.run4you.common.response.ApiResponse;
import com.run4you.report.dto.ReportCreateRequest;
import com.run4you.report.dto.ReportResponse;
import com.run4you.report.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 정비 리포트 API. 응답은 팀 공통 포맷(ApiResponse)으로 감싼다.
 *  POST /api/reports        리포트 작성(부품 단가 검증 + 비용 합산)
 *  GET  /api/reports        리포트 목록 (engineerId 필터 가능)
 *  GET  /api/reports/{id}   리포트 상세
 */
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    public ResponseEntity<ApiResponse<ReportResponse>> create(@Valid @RequestBody ReportCreateRequest request) {
        ReportResponse response = reportService.createReport(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of(response, "정비 리포트가 작성되었습니다."));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ReportResponse>>> list(@RequestParam(required = false) Long engineerId) {
        return ResponseEntity.ok(ApiResponse.success(reportService.listReports(engineerId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ReportResponse>> detail(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(reportService.getReport(id)));
    }
}
