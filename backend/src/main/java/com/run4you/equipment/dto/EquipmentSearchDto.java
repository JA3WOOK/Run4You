package com.run4you.equipment.dto;

import com.run4you.equipment.entity.EquipmentCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder

// 기자재 현황 - 카테고리 필터 + 검색 기능
public class EquipmentSearchDto {
    private EquipmentCategory category;
    private String keyword;
}
