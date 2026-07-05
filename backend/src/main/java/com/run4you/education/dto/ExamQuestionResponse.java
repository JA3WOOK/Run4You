package com.run4you.education.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/** 응시용 문항 — 정답(answer)은 절대 내려주지 않는다 */
@Getter
@Builder
public class ExamQuestionResponse {
    private Long questionId;
    private String question;
    private List<String> choices;
    private Integer score;
}
