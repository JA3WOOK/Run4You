package com.run4you.common.exception;

/** AS 요청이 이미 배정되어 수락 불가능할 때 */
public class AlreadyAssignedException extends RuntimeException {
    public AlreadyAssignedException(String message) { super(message); }
}
