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
import org.springframework.web.client.RestClient;

import java.util.Map;

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
    public void analyzeAndSave(Long asRequestId, String symptom, String faultCategory) {
        try {
            String prompt = buildPrompt(symptom, faultCategory);

            Map<String, Object> body = Map.of(
                    "contents", new Object[]{
                            Map.of("parts", new Object[]{ Map.of("text", prompt) })
                    }
            );

            String response = restClient.post()
                    .uri("/models/gemini-2.5-flash-lite:generateContent?key=" + apiKey)
                    .body(body)
                    .retrieve()
                    .body(String.class);

            AiDiagnosisResult result = parseResponse(response);

            // 1) as_requests에 결과 반영
            AsRequest asRequest = asRequestRepository.findById(asRequestId)
                    .orElse(null);
            if (asRequest != null) {
                asRequest.applyAiDiagnosis(result.errorCode(), result.causeDescription(), result.recommendedParts());
                asRequestRepository.save(asRequest);
                log.info("AI 진단 완료 (asRequestId={}, errorCode={})", asRequestId, result.errorCode());
            }

            // 2) error_codes 마스터에 신규 코드면 누적 저장
            saveErrorCodeIfAbsent(result, faultCategory);

        } catch (Exception e) {
            log.error("AI 진단 분석 실패 (asRequestId={})", asRequestId, e);
        }
    }

    // error_codes에 없는 코드면 AI 결과 그대로 신규 저장
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

    private String buildPrompt(String symptom, String faultCategory) {
        return """
                너는 카페 기자재 A/S 전문가야.
                아래 증상을 보고 정형 에러코드, 예상 원인, 지참 권장 부품을 JSON으로만 답해.
                다른 설명 없이 JSON만 출력해.

                형식: {"errorCode": "...", "causeDescription": "...", "recommendedParts": "부품1,부품2,부품3"}

                고장 카테고리: %s
                증상: %s
                """.formatted(faultCategory, symptom);
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