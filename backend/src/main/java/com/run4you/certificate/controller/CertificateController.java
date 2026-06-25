package com.run4you.certificate.controller;

import com.run4you.certificate.dto.CertificateIssueRequest;
import com.run4you.certificate.dto.CertificateResponse;
import com.run4you.certificate.service.HealthCertificateService;
import com.run4you.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 기기 건강 진단서 API.
 *  POST /api/certificates/issue              진단서 발급(§20 등급 산정)
 *  GET  /api/certificates                    전체 목록
 *  GET  /api/certificates/{id}               상세
 *  GET  /api/certificates/equipment/{eqId}   기자재별 진단서 이력
 */
@RestController
@RequestMapping("/api/certificates")
@RequiredArgsConstructor
public class CertificateController {

    private final HealthCertificateService certificateService;

    @PostMapping("/issue")
    public ResponseEntity<ApiResponse<CertificateResponse>> issue(
            @Valid @RequestBody CertificateIssueRequest request) {
        CertificateResponse res = certificateService.issue(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of(res, "진단서가 발급되었습니다."));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CertificateResponse>>> list() {
        return ResponseEntity.ok(ApiResponse.success(certificateService.list()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CertificateResponse>> detail(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(certificateService.get(id)));
    }

    @GetMapping("/equipment/{equipmentId}")
    public ResponseEntity<ApiResponse<List<CertificateResponse>>> byEquipment(@PathVariable Long equipmentId) {
        return ResponseEntity.ok(ApiResponse.success(certificateService.byEquipment(equipmentId)));
    }
}
