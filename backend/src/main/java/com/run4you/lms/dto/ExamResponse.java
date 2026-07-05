package com.run4you.lms.dto;

import com.run4you.lms.entity.Exam;
import lombok.Getter;

import java.util.List;

/** 관리자용 시험 응답 — 문항 정답까지 포함해서 편집 화면에 그대로 사용 */
@Getter
public class ExamResponse {
    private final Long id;
    private final Long courseId;
    private final String title;
    private final Integer passScore;   // Course.passScore
    private final List<ExamQuestionResponse> questions;

    public ExamResponse(Exam exam) {
        this.id = exam.getId();
        this.courseId = exam.getCourse().getId();
        this.title = exam.getTitle();
        this.passScore = exam.getCourse().getPassScore();
        this.questions = exam.getQuestions().stream().map(ExamQuestionResponse::new).toList();
    }
}
