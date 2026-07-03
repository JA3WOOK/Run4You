package com.run4you.asrequest.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class ReviewCreateRequest {

    @NotNull
    private Long asRequestId;

    @NotNull
    @Min(1)
    @Max(5)
    private Integer rating;

    private String comment;
}