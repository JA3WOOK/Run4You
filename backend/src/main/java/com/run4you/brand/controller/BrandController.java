package com.run4you.brand.controller;

import com.run4you.brand.dto.BrandResponse;
import com.run4you.brand.dto.BrandUpdateCommissionRequest;
import com.run4you.brand.service.BrandService;
import com.run4you.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/brands")
@RequiredArgsConstructor
public class BrandController {

    private final BrandService brandService;

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<BrandResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(brandService.getAll()));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<BrandResponse>>> getActive() {
        return ResponseEntity.ok(ApiResponse.success(brandService.getActive()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRAND_ADMIN')")
    public ResponseEntity<ApiResponse<BrandResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(brandService.getById(id)));
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<BrandResponse>> approve(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(brandService.approve(id)));
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<BrandResponse>> reject(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(brandService.reject(id)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        brandService.deleteBrand(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PatchMapping("/{id}/commission-rate")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<BrandResponse>> updateCommissionRate(
            @PathVariable Long id,
            @Valid @RequestBody BrandUpdateCommissionRequest request) {
        return ResponseEntity.ok(ApiResponse.success(brandService.updateCommissionRate(id, request)));
    }
}
