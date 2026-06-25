package com.run4you.asrequest.dto;

import com.run4you.asrequest.entity.AsStatus;
import com.run4you.equipment.entity.EquipmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder

// 기자재 현황 - 이력 보기 모달 전체 구성
public class AsRequestHistoryDto {

    // 상단 기자재 정보
    private String name;
    private String modelName;
    private String serialNo;
    private LocalDate purchasedAt; // 구매일
    private LocalDateTime lastRepairAt; // 최근 수리일
    private String storeName;
    private EquipmentStatus status;

    // 수리 이력 1건
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RepairHistoryItem {
        private LocalDateTime completedAt;
        private String errorCode;
        private String symptom; // 고장 증상
        private BigDecimal totalCost;
        private AsStatus status;
        private String diagnosis; // 정비 의견
        private Long repairReportId; // 진단서/영수증 연결용 ID
    }

    // 수리 이력 전체 목록
    private List<RepairHistoryItem> repairHistoryItems;

    // 하단 요약 - 총 수리 횟수 및 수리 비용
    private int totalRepairCount;
    private BigDecimal totalRepairCost;

}
