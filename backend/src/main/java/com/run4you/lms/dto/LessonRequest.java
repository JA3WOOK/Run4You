package com.run4you.lms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class LessonRequest {
    @NotBlank
    private String title;
    private String videoUrl;
    private Integer durationSeconds;
    private Integer sortOrder;
    private String content;
    @NotNull
    private Integer orderIndex;
}
