package com.run4you.lms.dto;

import com.run4you.lms.entity.Manual;
import com.run4you.lms.entity.ManualType;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ManualResponse {
    private final Long id;
    private final String title;
    private final String content;
    private final ManualType manualType;
    private final String faultCategory;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public ManualResponse(Manual manual) {
        this.id = manual.getId();
        this.title = manual.getTitle();
        this.content = manual.getContent();
        this.manualType = manual.getManualType();
        this.faultCategory = manual.getFaultCategory();
        this.createdAt = manual.getCreatedAt();
        this.updatedAt = manual.getUpdatedAt();
    }
}
