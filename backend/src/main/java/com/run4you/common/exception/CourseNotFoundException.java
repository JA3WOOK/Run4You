package com.run4you.common.exception;

/** ※ common.exception 패키지에 기존 예외 컨벤션(예: RuntimeException 상속)이 있다면 그에 맞춰 주세요. */
public class CourseNotFoundException extends RuntimeException {
    public CourseNotFoundException(String message) { super(message); }
}
