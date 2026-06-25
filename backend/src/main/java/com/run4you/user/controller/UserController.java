package com.run4you.user.controller;

import com.run4you.common.response.ApiResponse;
import com.run4you.user.dto.UserResponse;
import com.run4you.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(userService.getAll()));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRAND_ADMIN')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getPendingUsers(
            @AuthenticationPrincipal String email) {
        return ResponseEntity.ok(ApiResponse.success(userService.getPendingUsers(email)));
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRAND_ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> approve(
            @PathVariable Long id,
            @AuthenticationPrincipal String email) {
        return ResponseEntity.ok(ApiResponse.success(userService.approve(id, email)));
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRAND_ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> reject(
            @PathVariable Long id,
            @AuthenticationPrincipal String email) {
        return ResponseEntity.ok(ApiResponse.success(userService.reject(id, email)));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> deactivate(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(userService.deactivate(id)));
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> activate(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(userService.activate(id)));
    }
}
