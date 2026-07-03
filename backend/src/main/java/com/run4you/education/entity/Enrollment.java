package com.run4you.education.entity;

import com.run4you.common.enums.EnrollmentStatus;
import com.run4you.lms.entity.Course;
import com.run4you.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 엔지니어 수강·진도율  */
@Entity
@Table(name = "enrollments", uniqueConstraints = {
        @UniqueConstraint(name = "uk_enrollment_engineer_course", columnNames = {"engineer_id", "course_id"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "engineer_id", nullable = false)
    private User engineer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "progress_rate", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal progressRate = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EnrollmentStatus status = EnrollmentStatus.NOT_STARTED;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    /** 진도율 갱신 + 상태 자동 전이 (NOT_STARTED → IN_PROGRESS → COMPLETED) */
    public void updateProgress(BigDecimal newRate) {
        this.progressRate = newRate;
        this.updatedAt = LocalDateTime.now();

        if (newRate.compareTo(BigDecimal.valueOf(100)) >= 0) {
            this.status = EnrollmentStatus.COMPLETED;
            if (this.completedAt == null) {
                this.completedAt = LocalDateTime.now();
            }
        } else if (newRate.compareTo(BigDecimal.ZERO) > 0) {
            this.status = EnrollmentStatus.IN_PROGRESS;
        }
    }
}
