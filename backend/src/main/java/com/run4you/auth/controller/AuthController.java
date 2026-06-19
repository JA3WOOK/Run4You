package com.run4you.auth.controller;

import com.run4you.auth.dto.LoginRequest;
import com.run4you.auth.dto.SignupRequest;
import com.run4you.auth.dto.TokenResponse;
import com.run4you.auth.service.AuthService;
import com.run4you.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Void>> signup(@Valid @RequestBody SignupRequest request) {
        authService.signup(request);
        return ResponseEntity.ok(ApiResponse.of(200, "회원가입이 완료되었습니다. 관리자 승인 후 로그인 가능합니다.", null));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(@Valid @RequestBody LoginRequest request) {
        TokenResponse token = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(token));
    }

    @PostMapping("/reissue")
    public ResponseEntity<ApiResponse<TokenResponse>> reissue(@RequestHeader("Refresh-Token") String refreshToken) {
        TokenResponse token = authService.reissue(refreshToken);
        return ResponseEntity.ok(ApiResponse.success(token));
    }
}
