package com.run4you.lms.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

/** 시험 문항 (ERD 8-6. exam_questions) */
@Entity
@Table(name = "exam_questions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ExamQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String question;

    /** 보기 목록 (JSON 배열 컬럼 <-> List<String> 변환) */
    @Convert(converter = StringListJsonConverter.class)
    @Column(nullable = false, columnDefinition = "JSON")
    private List<String> choices;

    @Column(nullable = false, length = 10)
    private String answer; // 정답 보기 (예: "2")

    @Column(nullable = false)
    private Integer score;

    public void update(String question, List<String> choices, String answer, Integer score) {
        if (question != null && !question.isBlank()) this.question = question;
        if (choices != null && !choices.isEmpty()) this.choices = choices;
        if (answer != null && !answer.isBlank()) this.answer = answer;
        if (score != null) this.score = score;
    }
}
