package com.run4you.dispatch.dto;

import com.run4you.dispatch.entity.DispatchStatusHistory;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
public class DispatchHistoryItemResponse {
    private String status;
    private LocalDateTime changedAt;

    public static DispatchHistoryItemResponse of(DispatchStatusHistory h) {
        return DispatchHistoryItemResponse.builder()
                .status(h.getStatus().name())
                .changedAt(h.getChangedAt())
                .build();
    }
}