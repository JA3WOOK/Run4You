package com.run4you.lms.dto;

import com.run4you.lms.entity.Lesson;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class LessonResponse {
    private final Long id;
    private final Long courseId;
    private final String title;
    private final String videoUrl;
    private final Integer durationSeconds;
    private final Integer sortOrder;
    private final String content;
    private final Integer orderIndex;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public LessonResponse(Lesson lesson) {
        this.id = lesson.getId();
        this.courseId = lesson.getCourse().getId();
        this.title = lesson.getTitle();
        this.videoUrl = lesson.getVideoUrl();
        this.durationSeconds = lesson.getDurationSeconds();
        this.sortOrder = lesson.getSortOrder();
        this.content = lesson.getContent();
        this.orderIndex = lesson.getOrderIndex();
        this.createdAt = lesson.getCreatedAt();
        this.updatedAt = lesson.getUpdatedAt();
    }
}
