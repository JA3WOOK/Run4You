package com.run4you.asrequest.service;

public record AiDiagnosisResult(
        String errorCode,
        String causeDescription,
        String recommendedParts
) {
    public static AiDiagnosisResult empty() {
        return new AiDiagnosisResult(null, null, null);
    }
}