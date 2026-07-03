package com.run4you.education.entity;

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
    @Convert(converter = com.run4you.education.entity.StringListJsonConverter.class)
    @Column(nullable = false, columnDefinition = "JSON")
    private List<String> choices;

    @Column(nullable = false, length = 10)
    private String answer; // 정답 보기 (예: "2")

    @Column(nullable = false)
    private Integer score;
}
