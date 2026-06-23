package com.run4you.common.exception;

import com.run4you.common.response.ApiResponse;
import com.run4you.common.response.ApiResponse.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 이미 배정됨 — 409 Conflict */
    @ExceptionHandler(AlreadyAssignedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAlreadyAssigned(AlreadyAssignedException e) {
        log.warn("[예외] AlreadyAssigned: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(ErrorCode.ALREADY_ASSIGNED));
    }

    /** 분산 락 획득 실패 — 409 Conflict */
    @ExceptionHandler(LockAcquisitionFailedException.class)
    public ResponseEntity<ApiResponse<Void>> handleLockFailed(LockAcquisitionFailedException e) {
        log.warn("[예외] LockFailed: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(ErrorCode.LOCK_FAILED));
    }

    /** 엔지니어 없음 — 404 Not Found */
    @ExceptionHandler(EngineerNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleEngineerNotFound(EngineerNotFoundException e) {
        log.warn("[예외] EngineerNotFound: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ErrorCode.ENGINEER_NOT_FOUND));
    }

    /** AS 요청 없음 — 404 Not Found */
    @ExceptionHandler(AsRequestNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleAsRequestNotFound(AsRequestNotFoundException e) {
        log.warn("[예외] AsRequestNotFound: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ErrorCode.AS_REQUEST_NOT_FOUND));
    }

    /** 서비스 반경 초과 — 400 Bad Request */
    @ExceptionHandler(OutOfServiceRadiusException.class)
    public ResponseEntity<ApiResponse<Void>> handleOutOfRadius(OutOfServiceRadiusException e) {
        log.warn("[예외] OutOfRadius: {}", e.getMessage());
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(ErrorCode.OUT_OF_SERVICE_RADIUS));
    }

    /** IllegalArgumentException — 400 Bad Request */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException e) {
        log.warn("[예외] IllegalArgument: {}", e.getMessage());
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
    }

    /** 상태 전이 불가 — 422 Unprocessable Entity */
    @ExceptionHandler(InvalidStatusTransitionException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidTransition(InvalidStatusTransitionException e) {
        log.warn("[예외] InvalidTransition: {}", e.getMessage());
        return ResponseEntity.unprocessableEntity()
                .body(ApiResponse.error(e.getMessage(), ErrorCode.INVALID_STATUS_TRANSITION.name()));
    }

    /** 권한 없음 — 403 Forbidden */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException e) {
        log.warn("[예외] Forbidden: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(ErrorCode.FORBIDDEN));
    }

    /** IllegalStateException — 403 Forbidden */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalState(IllegalStateException e) {
        log.warn("[예외] IllegalState: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(e.getMessage()));
    }

    /** 입력값 검증 실패 — 400 Bad Request */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("입력값이 올바르지 않습니다.");
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(message, ErrorCode.VALIDATION_FAILED.name()));
    }

    /** 그 외 예상치 못한 예외 — 500 Internal Server Error */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneral(Exception e) {
        log.error("[예외] Unhandled exception", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(ErrorCode.INTERNAL_ERROR));
    }
}