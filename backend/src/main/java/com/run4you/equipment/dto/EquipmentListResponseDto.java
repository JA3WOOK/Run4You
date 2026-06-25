package com.run4you.equipment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder

// 기자재 현황 - 상단 카운트 요약 + 목록
public class EquipmentListResponseDto {
    private long totalCount;
    private long operationalCount;
    private long faultyCount;
    private long repairingCount;
    private List<EquipmentResponseDto> equipments;
}
