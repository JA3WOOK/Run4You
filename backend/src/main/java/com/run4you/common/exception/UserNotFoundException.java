package com.run4you.common.exception;

/** ※ 이미 UserNotFoundException(또는 동일 역할의 예외)이 있다면 새로 만들지 말고 재사용하세요. */
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) { super(message); }
}
