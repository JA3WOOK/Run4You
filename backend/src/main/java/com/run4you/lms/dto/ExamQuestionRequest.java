package com.run4you.lms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class ExamQuestionRequest {

    @NotBlank
    private String question;

    @NotEmpty
    private List<String> choices;

    /** 정답 보기 번호 (1부터 시작, 문자열로 저장 — 예: "2") */
    @NotBlank
    private String answer;

    @NotNull
    private Integer score;
}
