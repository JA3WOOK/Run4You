package com.run4you.report.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.util.List;

/**
 * 정비 리포트 작성 요청.
 * assignmentId/asRequestId/engineerId 는 앞 단계(팀원) 데이터의 ID 값으로 전달받는다.
 */
public record ReportCreateRequest(

        @NotNull(message = "배정 ID는 필수입니다.")
        Long assignmentId,

        @NotNull(message = "접수 ID는 필수입니다.")
        Long asRequestId,

        @NotNull(message = "엔지니어 ID는 필수입니다.")
        Long engineerId,

        Long equipmentId,

        @NotNull(message = "공임비는 필수입니다.")
        @PositiveOrZero(message = "공임비는 0 이상이어야 합니다.")
        BigDecimal laborCost,

        String diagnosis,

        @Valid
        List<PartLineRequest> parts
) {
}
