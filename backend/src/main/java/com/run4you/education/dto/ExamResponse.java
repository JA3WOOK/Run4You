package com.run4you.education.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ExamResponse {
    private Long examId;
    private Long courseId;
    private String title;
    private Integer passScore;    // Course.passScore 를 그대로 사용
    private Integer totalScore;
    private List<ExamQuestionResponse> questions;
}
