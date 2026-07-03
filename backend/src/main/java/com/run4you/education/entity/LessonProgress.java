package com.run4you.education.entity;

import com.run4you.lms.entity.Lesson;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/** 차시별 진도 기록 */
@Entity
@Table(name = "lesson_progress", uniqueConstraints = {
        @UniqueConstraint(name = "uk_progress_enrollment_lesson", columnNames = {"enrollment_id", "lesson_id"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class LessonProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enrollment_id", nullable = false)
    private Enrollment enrollment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    @Column(name = "watched_seconds", nullable = false)
    @Builder.Default
    private Integer watchedSeconds = 0;

    @Column(name = "progress_rate", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal progressRate = BigDecimal.ZERO;

    @Column(name = "is_completed", nullable = false)
    @Builder.Default
    private Boolean isCompleted = false;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /** 시청 시간 갱신. 되감기로 인한 진도율 역행은 막는다(최댓값 유지). */
    public void applyWatchedSeconds(int newlyWatchedSeconds, int durationSeconds) {
        this.watchedSeconds = Math.max(this.watchedSeconds, Math.min(newlyWatchedSeconds, durationSeconds));
        BigDecimal rate = durationSeconds == 0
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(this.watchedSeconds)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(durationSeconds), 2, java.math.RoundingMode.HALF_UP);
        if (rate.compareTo(BigDecimal.valueOf(100)) > 0) rate = BigDecimal.valueOf(100);
        this.progressRate = rate;
        this.isCompleted = rate.compareTo(BigDecimal.valueOf(100)) >= 0;
        this.updatedAt = LocalDateTime.now();
    }
}
