package com.run4you.education.entity;

import com.run4you.lms.entity.Course;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 코스 필기시험 — courses : exams = 1:1
 * ※ 합격 기준 점수는 별도로 두지 않고 Course.passScore를 그대로 사용.
 *
 */
@Entity
@Table(name = "exams")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Exam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false, unique = true)
    private Course course;

    @Column(nullable = false, length = 100)
    private String title;

    @OneToMany(mappedBy = "exam", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ExamQuestion> questions = new ArrayList<>();
}
