package com.run4you.lms.dto;

import com.run4you.lms.entity.ExamQuestion;
import lombok.Getter;

import java.util.List;

/** 관리자용 문항 응답 — 정답(answer)을 포함한다 (엔지니어 응시용 education.dto.ExamQuestionResponse와는 별개) */
@Getter
public class ExamQuestionResponse {
    private final Long id;
    private final String question;
    private final List<String> choices;
    private final String answer;
    private final Integer score;

    public ExamQuestionResponse(ExamQuestion q) {
        this.id = q.getId();
        this.question = q.getQuestion();
        this.choices = q.getChoices();
        this.answer = q.getAnswer();
        this.score = q.getScore();
    }
}
