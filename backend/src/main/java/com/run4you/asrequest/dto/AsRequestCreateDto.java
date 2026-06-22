package com.run4you.asrequest.dto;

import com.run4you.asrequest.entity.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder

// 긴급 A/S 접수 입력 폼
public class AsRequestCreateDto {

    @NotNull(message = "해당 기자재를 선택해 주세요")
    private Long equipmentId;

    @NotNull(message = "우선순위를 선택해 주세요")
    private Priority priority;

    private String errorCode;

    @NotBlank(message = "고장 증상을 입력해 주세요")
    private String symptom;

}
