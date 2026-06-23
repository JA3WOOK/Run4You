package com.run4you.asrequest.dto;

import com.run4you.equipment.entity.EquipmentCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder

// 진단서 및 영수증 목록 - 날짜 필터 + 카테고리 드롭다운
public class ReceiptSearchDto {

    private LocalDate startDate;        // 시작일 AsRequest
    private LocalDate endDate;          // 종료일 AsRequest
    private EquipmentCategory category; // 카테고리 드롭다운 Equipment
}
