package com.run4you.lms.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ExamRequest {

    @NotBlank
    private String title;
}
