package com.run4you.dispatch.exception;

import com.run4you.dispatch.domain.DispatchStatus;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 출동 관제 도메인 예외. HTTP 상태와 코드를 함께 보유해 핸들러에서 일관 응답한다.
 */
@Getter
public class DispatchException extends RuntimeException {

    private final HttpStatus status;
    private final String code;

    public DispatchException(HttpStatus status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }

    public static DispatchException assignmentNotFound(Long id) {
        return new DispatchException(HttpStatus.NOT_FOUND, "ASSIGNMENT_NOT_FOUND",
                "배정을 찾을 수 없습니다: " + id);
    }

    public static DispatchException forbidden(String message) {
        return new DispatchException(HttpStatus.FORBIDDEN, "DISPATCH_FORBIDDEN", message);
    }

    public static DispatchException invalidTransition(DispatchStatus from, DispatchStatus to) {
        return new DispatchException(HttpStatus.CONFLICT, "INVALID_STATUS_TRANSITION",
                "허용되지 않는 상태 전이입니다: " + from + " → " + to);
    }
}
