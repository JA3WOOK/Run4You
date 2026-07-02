package com.run4you.lms.dto;

import com.run4you.lms.entity.ManualType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class ManualRequest {
    @NotBlank
    private String title;
    @NotBlank
    private String content;
    @NotNull
    private ManualType manualType;
    private String faultCategory;
}
