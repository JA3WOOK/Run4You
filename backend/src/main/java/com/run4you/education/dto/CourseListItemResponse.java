package com.run4you.education.dto;

import com.run4you.common.enums.EnrollmentStatus;
import com.run4you.lms.entity.CourseLevel;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

/** 엔지니어 코스 목록 아이템 — 내 수강 진도율 포함 */
@Getter
@Builder
public class CourseListItemResponse {
    private Long courseId;
    private String title;
    private String gradeLabel;      // Course.grade (표시용 String, 예: "초급")
    private CourseLevel level;      // Course.level (필터/등급상향 판단용 enum)
    private String category;
    private String description;
    private int lessonCount;
    private BigDecimal myProgressRate;
    private EnrollmentStatus myStatus;
    private boolean hasExam;
}
