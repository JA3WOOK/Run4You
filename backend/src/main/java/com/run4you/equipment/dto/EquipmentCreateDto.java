package com.run4you.equipment.dto;

import com.run4you.equipment.entity.EquipmentCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder

// 기자재 현황 - 기자재 등록 폼
public class EquipmentCreateDto {

    @NotBlank(message = "기자재명을 입력해주세요")
    private String name;

    @NotNull(message = "카테고리를 선택해주세요")
    private EquipmentCategory category;

    @NotBlank(message = "모델명을 입력해주세요")
    private String modelName;

    @NotBlank(message = "제조사를 입력해주세요")
    private String manufacturer;

    @NotBlank(message = "시리얼 번호를 입력해 주세요")
    private String serialNo;

    private LocalDate purchasedAt;
}
