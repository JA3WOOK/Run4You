package com.run4you.equipment.dto;

import com.run4you.equipment.entity.EquipmentCategory;
import com.run4you.equipment.entity.EquipmentStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class AdminEquipmentResponseDto {
    private Long id;
    private String storeName;
    private String name;
    private String modelName;
    private EquipmentCategory category;
    private EquipmentStatus status;
    private LocalDate purchasedAt;
    private LocalDate nextInspectionDate;
}