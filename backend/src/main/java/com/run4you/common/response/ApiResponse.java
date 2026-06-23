package com.run4you.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final boolean success;
    private final T data;
    private final String message;
    private final String code;
    private final LocalDateTime timestamp;

    private ApiResponse(boolean success, T data, String message, String code) {
        this.success   = success;
        this.data      = data;
        this.message   = message;
        this.code      = code;
        this.timestamp = LocalDateTime.now();
    }

    // ─── of (success 축약형) ──────────────────────────────────────

    /** ApiResponse.of(data) — success(data) 축약형 */
    public static <T> ApiResponse<T> of(T data) {
        return new ApiResponse<>(true, data, null, null);
    }

    /** ApiResponse.of(data, message) — 메시지 포함 성공 */
    public static <T> ApiResponse<T> of(T data, String message) {
        return new ApiResponse<>(true, data, message, null);
    }

    // ─── success ─────────────────────────────────────────────────

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null, null);
    }

    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(true, null, null, null);
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, data, message, null);
    }

    // ─── error ───────────────────────────────────────────────────

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, null, message, null);
    }

    public static <T> ApiResponse<T> error(String message, String code) {
        return new ApiResponse<>(false, null, message, code);
    }

    public static <T> ApiResponse<T> error(ErrorCode errorCode) {
        return new ApiResponse<>(false, null, errorCode.getMessage(), errorCode.name());
    }

    // ─── ErrorCode ───────────────────────────────────────────────

    public enum ErrorCode {

        ALREADY_ASSIGNED("이미 처리된 요청입니다."),
        LOCK_FAILED("현재 처리 중입니다. 잠시 후 다시 시도해주세요."),
        OUT_OF_SERVICE_RADIUS("서비스 반경 밖의 요청입니다."),
        INVALID_STATUS_TRANSITION("허용되지 않는 상태 전이입니다."),

        ENGINEER_NOT_FOUND("엔지니어를 찾을 수 없습니다."),
        AS_REQUEST_NOT_FOUND("AS 요청을 찾을 수 없습니다."),

        VALIDATION_FAILED("입력값이 올바르지 않습니다."),
        UNAUTHORIZED("인증이 필요합니다."),
        FORBIDDEN("접근 권한이 없습니다."),
        INTERNAL_ERROR("서버 내부 오류가 발생했습니다.");

        private final String message;

        ErrorCode(String message) { this.message = message; }

        public String getMessage() { return message; }
    }
}