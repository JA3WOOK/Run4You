package com.run4you.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 런포유 공통 API 응답 래퍼

   성공 응답:
     { "success": true, "data": { ... }, "timestamp": "..." }

   실패 응답:
     { "success": false, "message": "...", "code": "ALREADY_ASSIGNED", "timestamp": "..." }
 */
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL) // null 필드는 JSON에서 제외
public class ApiResponse<T> {

    private final boolean success;

    /** 성공 시 응답 데이터 */
    private final T data;

    /** 실패 시 사람이 읽을 수 있는 에러 메시지 */
    private final String message;

    /** 실패 시 클라이언트 핸들링용 에러 코드 */
    private final String code;

    /** 응답 생성 시각 */
    private final LocalDateTime timestamp;

    // ─── 생성자 (private) ─────────────────────────────────────────

    private ApiResponse(boolean success, T data, String message, String code) {
        this.success = success;
        this.data = data;
        this.message = message;
        this.code = code;
        this.timestamp = LocalDateTime.now();
    }

    // ─── 성공 팩토리 ──────────────────────────────────────────────

    /** 데이터 있는 성공 응답 */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null, null);
    }

    /** 데이터 없는 성공 응답 (204 No Content 대신 사용 가능) */
    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(true, null, null, null);
    }

    /** 메시지 포함 성공 응답 (생성·수락 완료 등 안내 문구가 필요할 때) */
    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, data, message, null);
    }

    // ─── 실패 팩토리 ──────────────────────────────────────────────

    /** 메시지만 있는 에러 응답 */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, null, message, null);
    }

    /**
     * 메시지 + 에러 코드 에러 응답
     * 클라이언트가 코드 기반으로 분기 처리할 때 사용
     */
    public static <T> ApiResponse<T> error(String message, String code) {
        return new ApiResponse<>(false, null, message, code);
    }

    /** ErrorCode enum과 함께 사용하는 팩토리 */
    public static <T> ApiResponse<T> error(ErrorCode errorCode) {
        return new ApiResponse<>(false, null, errorCode.getMessage(), errorCode.name());
    }

    // ─── 에러 코드 enum ───────────────────────────────────────────

    /**
     * 클라이언트 핸들링용 에러 코드 정의
     * GlobalExceptionHandler에서 참조하여 일관된 코드 반환
     */
    public enum ErrorCode {

        // 매칭·배정
        ALREADY_ASSIGNED("이미 처리된 요청입니다."),
        LOCK_FAILED("현재 처리 중입니다. 잠시 후 다시 시도해주세요."),
        OUT_OF_SERVICE_RADIUS("서비스 반경 밖의 요청입니다."),
        INVALID_STATUS_TRANSITION("허용되지 않는 상태 전이입니다."),

        // 리소스 없음
        ENGINEER_NOT_FOUND("엔지니어를 찾을 수 없습니다."),
        AS_REQUEST_NOT_FOUND("AS 요청을 찾을 수 없습니다."),

        // 공통
        VALIDATION_FAILED("입력값이 올바르지 않습니다."),
        UNAUTHORIZED("인증이 필요합니다."),
        FORBIDDEN("접근 권한이 없습니다."),
        INTERNAL_ERROR("서버 내부 오류가 발생했습니다.");

        private final String message;

        ErrorCode(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }
}