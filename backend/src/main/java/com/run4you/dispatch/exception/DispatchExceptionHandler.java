package com.run4you.dispatch.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 출동 관제 도메인 예외 응답 핸들러.
 * (프로젝트에 공통(common) GlobalExceptionHandler 가 도입되면 그쪽으로 통합 가능)
 */
@RestControllerAdvice(basePackages = "com.runforyou.dispatch")
public class DispatchExceptionHandler {

    @ExceptionHandler(DispatchException.class)
    public ResponseEntity<Map<String, Object>> handle(DispatchException e) {
        return ResponseEntity.status(e.getStatus()).body(body(e.getCode(), e.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handle(AccessDeniedException e) {
        return ResponseEntity.status(403).body(body("ACCESS_DENIED", e.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handle(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(body("BAD_REQUEST", e.getMessage()));
    }

    private Map<String, Object> body(String code, String message) {
        return Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "code", code,
                "message", message
        );
    }
}
