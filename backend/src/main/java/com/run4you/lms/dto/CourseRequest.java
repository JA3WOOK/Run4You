package com.run4you.lms.dto;

import com.run4you.lms.entity.CourseLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class CourseRequest {
    @NotBlank
    private String title;
    private String description;
    @NotBlank
    private String grade;
    private String category;
    @NotNull
    private CourseLevel level;
    private String targetSpecialty;
    private Integer passScore;
}
