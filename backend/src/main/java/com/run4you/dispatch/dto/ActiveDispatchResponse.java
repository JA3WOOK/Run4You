package com.run4you.dispatch.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 통합 관제 대시보드 "실시간 출동 현황" 1건.
 *
 * <p>브랜드(또는 총괄)의 진행 중 출동을 한 행으로 조립한 읽기 전용 뷰.
 * 현재 상태는 assignments.status, ETA/좌표/시각은 dispatch_status_history 최신 행에서 온다.
 */
public record ActiveDispatchResponse(
        Long assignmentId,
        Long asRequestId,
        String status,             // DISPATCH_STATUS (ACCEPTED/DISPATCHED/ARRIVED/REPAIRING ...)
        String storeName,
        String engineerName,
        String engineerPhone,
        String equipmentName,
        String equipmentCategory,  // EQUIPMENT_CATEGORY (KIOSK/ESPRESSO/ICE_MAKER/REFRIGERATOR)
        String priority,           // EMERGENCY / NORMAL
        Integer etaMinutes,        // null 가능 (미산출)
        BigDecimal latitude,
        BigDecimal longitude,
        LocalDateTime changedAt
) {}
