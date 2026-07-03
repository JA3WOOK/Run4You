package com.run4you.education.dto;

import com.run4you.common.enums.EnrollmentStatus;
import com.run4you.lms.entity.CourseLevel;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class CourseDetailResponse {
    private Long courseId;
    private String title;
    private String gradeLabel;
    private CourseLevel level;
    private String category;
    private String description;
    private BigDecimal myProgressRate;
    private EnrollmentStatus myStatus;
    private boolean hasExam;
    private List<LessonResponse> lessons;
}
