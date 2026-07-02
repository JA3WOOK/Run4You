package com.run4you.lms.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "courses")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String grade;

    @Column
    private String category;

    @Column(nullable = false)
    private String status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CourseLevel level;

    @Column(name = "target_specialty")
    private String targetSpecialty;

    @Column(name = "pass_score")
    private Integer passScore;

    @Builder.Default
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Lesson> lessons = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public void update(String title, String description, String grade, String category,
                       CourseLevel level, String targetSpecialty, Integer passScore) {
        if (title != null && !title.isBlank()) this.title = title;
        if (description != null) this.description = description;
        if (grade != null && !grade.isBlank()) this.grade = grade;
        if (category != null) this.category = category;
        if (level != null) this.level = level;
        if (targetSpecialty != null) this.targetSpecialty = targetSpecialty;
        if (passScore != null) this.passScore = passScore;
    }
}
