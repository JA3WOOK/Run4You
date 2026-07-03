package com.run4you.education.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 차시 시청 진도 갱신 요청 — 프론트 동영상 플레이어가 주기적으로 호출 */
@Getter
@NoArgsConstructor
public class LessonProgressUpdateRequest {

    @NotNull
    @Min(0)
    private Integer watchedSeconds;
}
