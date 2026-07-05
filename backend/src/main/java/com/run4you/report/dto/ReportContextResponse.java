package com.run4you.report.dto;

public record ReportContextResponse(
        Long asRequestId,
        String storeName,
        String ownerName,
        String storeAddress,
        String equipmentName,
        String symptom,
        String errorCode
) {
}
