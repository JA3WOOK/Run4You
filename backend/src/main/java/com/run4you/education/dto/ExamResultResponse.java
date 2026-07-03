package com.run4you.education.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ExamResultResponse {
    private Long attemptId;
    private Integer score;
    private Integer passScore;
    private boolean passed;
    private boolean gradeUpgraded;   // 합격으로 인해 엔지니어 등급이 상향되었는지
    private String newGrade;         // 상향된 경우 등급명 (없으면 null)
}
