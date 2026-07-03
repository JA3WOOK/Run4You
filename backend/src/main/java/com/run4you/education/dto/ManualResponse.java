package com.run4you.education.dto;

import com.run4you.lms.entity.ManualType;
import lombok.Builder;
import lombok.Getter;

/** ※ 실제 Manual 엔티티에는 videoUrl 필드가 없어서 제외했습니다. */
@Getter
@Builder
public class ManualResponse {
    private Long manualId;
    private ManualType manualType;
    private String title;
    private String faultCategory;
    private String content;
}
