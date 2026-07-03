package com.run4you.education.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class ExamSubmitRequest {

    @NotEmpty
    @Valid
    private List<AnswerItem> answers;

    @Getter
    @NoArgsConstructor
    public static class AnswerItem {
        @NotNull
        private Long questionId;

        @NotNull
        private String answer;
    }
}
