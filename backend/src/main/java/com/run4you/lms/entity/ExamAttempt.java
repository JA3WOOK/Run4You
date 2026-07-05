package com.run4you.lms.entity;

import com.run4you.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/** 시험 응시 이력 (ERD 8-6. exam_attempts) */
@Entity
@Table(name = "exam_attempts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ExamAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "engineer_id", nullable = false)
    private User engineer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    @Column(nullable = false)
    private Integer score;

    @Column(nullable = false)
    private Boolean passed;

    @Column(name = "attempted_at", nullable = false)
    private LocalDateTime attemptedAt;

    @PrePersist
    void prePersist() {
        this.attemptedAt = LocalDateTime.now();
    }
}
