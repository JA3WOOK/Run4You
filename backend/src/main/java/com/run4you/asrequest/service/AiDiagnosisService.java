package com.run4you.asrequest.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.run4you.asrequest.entity.AsRequest;
import com.run4you.asrequest.entity.ErrorCode;
import com.run4you.asrequest.repository.AsRequestRepository;
import com.run4you.asrequest.repository.ErrorCodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiDiagnosisService {

    @Value("${gemini.api-key}")
    private String apiKey;

    private final AsRequestRepository asRequestRepository;
    private final ErrorCodeRepository errorCodeRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final RestClient restClient = RestClient.builder()
            .baseUrl("https://generativelanguage.googleapis.com/v1beta")
            .build();

    @Async
    @Transactional
    public void analyzeAndSave(Long asRequestId, String symptom, String faultCategory, String userErrorCode) {
        try {
            // 1) 사용자가 이미 유효한 에러코드를 입력했으면 AI 호출 없이 DB 값 그대로 사용
            if (userErrorCode != null && !userErrorCode.isBlank()) {
                Optional<ErrorCode> existing = errorCodeRepository.findByCode(userErrorCode.trim());
                if (existing.isPresent()) {
                    applyFromErrorCode(asRequestId, existing.get());
                    return;
                }
            }

            // 2) 완전히 동일한 증상+카테고리로 이미 AI 분석이 끝난 과거 접수가 있으면 그 결과 재사용
            Optional<AsRequest> exactMatch = asRequestRepository
                    .findFirstBySymptomAndFaultCategoryAndErrorCodeIsNotNullOrderByRequestedAtDesc(symptom, faultCategory);
            if (exactMatch.isPresent()) {
                AsRequest matched = exactMatch.get();
                applyFromPastRequest(asRequestId, matched);
                return;
            }

            // 3) 그 외에는 기존처럼 AI 분석 진행
            List<ErrorCode> existingCodes = errorCodeRepository.findByCategory(faultCategory);

            String prompt = buildPrompt(symptom, faultCategory, existingCodes);

            Map<String, Object> body = Map.of(
                    "contents", new Object[]{
                            Map.of("parts", new Object[]{ Map.of("text", prompt) })
                    }
            );

            String response = callGeminiWithRetry(body);

            AiDiagnosisResult result = parseResponse(response);

            AsRequest asRequest = asRequestRepository.findById(asRequestId).orElse(null);

            if (asRequest == null) {
                try {
                    Thread.sleep(300);
                } catch (InterruptedException ignored) {}
                asRequest = asRequestRepository.findById(asRequestId).orElse(null);
            }

            if (asRequest != null) {
                asRequest.applyAiDiagnosis(result.errorCode(), result.causeDescription(), result.recommendedParts());
                asRequestRepository.save(asRequest);
                log.info("AI 진단 완료 (asRequestId={}, errorCode={})", asRequestId, result.errorCode());
            } else {
                log.warn("AsRequest를 찾을 수 없어 AI 분석 결과 저장 실패 (asRequestId={})", asRequestId);
            }

            saveErrorCodeIfAbsent(result, faultCategory);

        } catch (Exception e) {
            log.error("AI 진단 분석 실패 (asRequestId={})", asRequestId, e);
        }
    }

    /**
     * 사용자가 입력한 에러코드가 이미 error_codes에 존재할 때,
     * AI 호출 없이 기존 값을 그대로 as_requests에 반영.
     */
    private void applyFromErrorCode(Long asRequestId, ErrorCode existing) {
        AsRequest asRequest = asRequestRepository.findById(asRequestId).orElse(null);
        if (asRequest == null) {
            log.warn("AsRequest를 찾을 수 없어 기존 에러코드 매칭 실패 (asRequestId={})", asRequestId);
            return;
        }
        asRequest.applyAiDiagnosis(existing.getCode(), existing.getDescription(), existing.getRecommendedParts());
        asRequestRepository.save(asRequest);
        log.info("기존 에러코드로 즉시 매칭 (asRequestId={}, code={})", asRequestId, existing.getCode());
    }

    /**
     * 완전히 동일한 증상+카테고리로 이미 처리된 과거 접수가 있을 때,
     * AI 호출 없이 그 결과를 그대로 재사용.
     */
    private void applyFromPastRequest(Long asRequestId, AsRequest pastRequest) {
        AsRequest asRequest = asRequestRepository.findById(asRequestId).orElse(null);
        if (asRequest == null) {
            log.warn("AsRequest를 찾을 수 없어 과거 접수 매칭 실패 (asRequestId={})", asRequestId);
            return;
        }
        asRequest.applyAiDiagnosis(
                pastRequest.getErrorCode(),
                pastRequest.getAiCauseDescription(),
                pastRequest.getAiRecommendedParts()
        );
        asRequestRepository.save(asRequest);
        log.info("동일 증상 과거 접수로 즉시 매칭 (asRequestId={}, code={}, sourceRequestId={})",
                asRequestId, pastRequest.getErrorCode(), pastRequest.getId());
    }

    /**
     * Gemini API 호출 — 503(과부하) 발생 시 최대 3회까지 재시도.
     * 1차 실패 시 1초, 2차 실패 시 2초 대기 후 재시도.
     */
    private String callGeminiWithRetry(Map<String, Object> body) {
        int maxAttempts = 3;
        long backoffMs = 1000;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return restClient.post()
                        .uri("/models/gemini-2.5-flash-lite:generateContent?key=" + apiKey)
                        .body(body)
                        .retrieve()
                        .body(String.class);
            } catch (HttpServerErrorException.ServiceUnavailable e) {
                log.warn("Gemini 503 (재시도 {}/{})", attempt, maxAttempts);
                if (attempt == maxAttempts) {
                    throw e;
                }
                try {
                    Thread.sleep(backoffMs * attempt);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw e;
                }
            }
        }
        throw new IllegalStateException("unreachable");
    }

    private void saveErrorCodeIfAbsent(AiDiagnosisResult result, String faultCategory) {
        if (result.errorCode() == null) return;

        boolean exists = errorCodeRepository.existsByCode(result.errorCode());
        if (!exists) {
            ErrorCode errorCode = ErrorCode.builder()
                    .code(result.errorCode())
                    .category(faultCategory)
                    .description(result.causeDescription())
                    .recommendedParts(result.recommendedParts())
                    .build();
            errorCodeRepository.save(errorCode);
            log.info("신규 에러코드 등록 (code={})", result.errorCode());
        }
    }

    private String buildPrompt(String symptom, String faultCategory, List<ErrorCode> existingCodes) {
        String prefix = mapCategoryToPrefix(faultCategory);

        String existingList = existingCodes.stream()
                .map(ec -> "- " + ec.getCode() + ": " + ec.getDescription())
                .collect(Collectors.joining("\n"));

        return """
                너는 카페 기자재 A/S 전문가야.
                아래 증상을 분석해서 에러코드, 예상 원인, 지참 권장 부품을 JSON으로만 답해.
                다른 설명 없이 JSON만 출력해.

                기존 에러코드 목록(같은 카테고리):
                %s

                규칙:
                - 위 목록 중 증상이 유사한 코드가 있으면 그 코드를 그대로 재사용해라.
                - 유사한 코드가 없을 때만 새로운 코드를 만들어라.
                - 새로 만들 경우 반드시 "%s-000" 형식으로 만들어라. (예: %s-001, %s-002)

                형식: {"errorCode": "...", "causeDescription": "...", "recommendedParts": "부품1,부품2,부품3"}

                고장 카테고리: %s
                증상: %s
                """.formatted(
                existingList.isEmpty() ? "(없음)" : existingList,
                prefix, prefix, prefix,
                faultCategory, symptom
        );
    }

    private String mapCategoryToPrefix(String category) {
        return switch (category) {
            case "ESPRESSO" -> "ESP";
            case "KIOSK" -> "KSK";
            case "ICE_MAKER" -> "ICE";
            case "REFRIGERATOR" -> "RFG";
            default -> "ETC";
        };
    }

    private AiDiagnosisResult parseResponse(String response) throws Exception {
        JsonNode root = objectMapper.readTree(response);
        String text = root.path("candidates").get(0)
                .path("content").path("parts").get(0)
                .path("text").asText();

        String cleaned = text.replaceAll("```json", "").replaceAll("```", "").trim();

        JsonNode result = objectMapper.readTree(cleaned);
        return new AiDiagnosisResult(
                result.path("errorCode").asText(null),
                result.path("causeDescription").asText(null),
                result.path("recommendedParts").asText(null)
        );
    }
}