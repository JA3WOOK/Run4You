package com.run4you.lms.dto;

import com.run4you.lms.entity.Course;
import com.run4you.lms.entity.CourseLevel;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class CourseResponse {
    private final Long id;
    private final String title;
    private final String description;
    private final String grade;
    private final String category;
    private final String status;
    private final CourseLevel level;
    private final String targetSpecialty;
    private final Integer passScore;
    private final int lessonCount;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public CourseResponse(Course course) {
        this.id = course.getId();
        this.title = course.getTitle();
        this.description = course.getDescription();
        this.grade = course.getGrade();
        this.category = course.getCategory();
        this.status = course.getStatus();
        this.level = course.getLevel();
        this.targetSpecialty = course.getTargetSpecialty();
        this.passScore = course.getPassScore();
        this.lessonCount = course.getLessons().size();
        this.createdAt = course.getCreatedAt();
        this.updatedAt = course.getUpdatedAt();
    }
}
