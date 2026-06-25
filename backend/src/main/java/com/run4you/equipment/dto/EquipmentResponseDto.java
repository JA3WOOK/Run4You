package com.run4you.equipment.dto;

import com.run4you.equipment.entity.EquipmentCategory;
import com.run4you.equipment.entity.EquipmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder

// 기자재 현황 - 기자재 카드 1개 화면 구성
public class EquipmentResponseDto {
    private Long id;
    private EquipmentCategory category;  // 카테고리 (아이콘용)
    private String name;  // 기기명
    private String modelName;
    private String serialNo;
    private EquipmentStatus status;
    private String errorCode;
    private LocalDate purchasedAt;
    private LocalDate nextInspectionDate;
}
