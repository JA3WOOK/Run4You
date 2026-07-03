package com.run4you.education.dto;

import com.run4you.common.enums.EnrollmentStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

/** 차시 진도 갱신 후 응답 — 차시 진도율 + 코스 전체 진도율 */
@Getter
@Builder
public class LessonProgressResponse {
    private Long lessonId;
    private BigDecimal lessonProgressRate;
    private boolean lessonCompleted;
    private BigDecimal courseProgressRate;
    private EnrollmentStatus courseStatus;
}
