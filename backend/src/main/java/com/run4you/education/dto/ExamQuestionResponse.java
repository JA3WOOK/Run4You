package com.run4you.education.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/** 응시용 문항  */
@Getter
@Builder
public class ExamQuestionResponse {
    private Long questionId;
    private String question;
    private List<String> choices;
    private Integer score;
}
