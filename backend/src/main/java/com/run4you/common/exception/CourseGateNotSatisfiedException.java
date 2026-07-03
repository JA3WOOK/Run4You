package com.run4you.common.exception;

/** 코스 진도율 100% 미달 등, 시험 응시/수리 진입 게이트 조건 미충족 시 발생 */
public class CourseGateNotSatisfiedException extends RuntimeException {
    public CourseGateNotSatisfiedException(String message) { super(message); }
}
