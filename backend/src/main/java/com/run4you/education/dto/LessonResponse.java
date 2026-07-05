package com.run4you.education.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class LessonResponse {
    private Long lessonId;
    private String title;
    private String videoUrl;
    private Integer durationSeconds;
    private Integer sortOrder;
    private String content;         // Lesson.content (보조 텍스트 가이드)
    private Integer watchedSeconds;
    private BigDecimal progressRate;
    private boolean completed;
}
