package com.example.ssauc.user.recommendation.service;

import com.example.ssauc.user.product.entity.Product;
import com.example.ssauc.user.recommendation.dto.RecommendationDto;
import com.example.ssauc.user.recommendation.repository.RecommendRepository;
import com.example.ssauc.user.recommendation.dto.RecommendationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;
import java.util.stream.Collectors;
@Service
@Slf4j
public class RecommendationService {

    private final RecommendRepository recommendRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${openai.api.key}")
    private String openaiApiKey;

    public RecommendationService(RecommendRepository recommendRepository, ObjectMapper objectMapper) {
        this.recommendRepository = recommendRepository;
        this.restTemplate = new RestTemplate();
        this.objectMapper = objectMapper;
    }

    /**
     * 현재 상품과 비슷한 상품 추천 메소드
     * @param currentProductId 현재 상세 페이지에서 보고 있는 상품 ID
     * @return 현재 상품과 비슷한 상품 리스트
     */
    public List<RecommendationDto> getSimilarProducts(Long currentProductId) {
        // 전체 후보 상품 조회 (필요하다면 조건 추가)
        List<RecommendationDto> products = recommendRepository.findRecommendProductsWithoutLogin();

        // 현재 상품 정보 추출
        RecommendationDto currentProduct = products.stream()
                .filter(p -> p.getProductId().equals(currentProductId))
                .findFirst()
                .orElse(null);
        if (currentProduct == null) {
            return Collections.emptyList();
        }

        // 현재 상품은 제외
        List<RecommendationDto> candidateProducts = products.stream()
                .filter(p -> !p.getProductId().equals(currentProductId))
                .collect(Collectors.toList());

        // 현재 상품 정보를 별도로 기재 (원하는 경우, productRepository에서 조회할 수 있음)
        // 여기서는 간단히 currentProductId만 활용
        String prompt = buildSimilarPrompt(candidateProducts, currentProduct);

        String gptResponse = callGptApi(prompt);

        List<Long> similarIds = parseRecommendedIds(gptResponse);

        // GPT에서 추천한 ID와 일치하는 상품만 필터링
        return candidateProducts.stream()
                .filter(p -> similarIds.contains(p.getProductId()))
                .collect(Collectors.toList());
    }

    /**
     * 비슷한 상품 추천을 위한 프롬프트 구성
     * @param products 후보 상품 목록
     * @return GPT에 전달할 프롬프트 문자열
     */

    private String buildSimilarPrompt(List<RecommendationDto> products, RecommendationDto currentProduct) {
        StringBuilder sb = new StringBuilder();
        sb.append("현재 상품 정보:\n");
        sb.append("상품명: ").append(currentProduct.getName()).append("\n");
        sb.append("설명: ").append(currentProduct.getDescription()).append("\n\n");
        sb.append("후보 상품 목록 중에서 현재 상품의 제목과 설명이 가장 유사한 상위 5개 상품의 제품 ID만을 오직 JSON 배열 형식으로 출력해 주세요.\n");
        sb.append("출력 예: [1,3,5,8,11]. 추가 텍스트나 설명은 전혀 포함하지 말아 주세요.\n");
        sb.append("후보 상품 목록 (각 행은 '제품ID, 상품명, 설명' 형식):\n");
        for (RecommendationDto product : products) {
            sb.append(product.getProductId())
                    .append(", ")
                    .append(product.getName())
                    .append(", ")
                    .append(product.getDescription())
                    .append("\n");
        }

        return sb.toString();
    }

    private String callGptApi(String prompt) {
        String url = "https://api.openai.com/v1/chat/completions";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + openaiApiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-3.5-turbo");

        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", "너는 상품 추천 전문가야.");
        messages.add(systemMessage);

        Map<String, String> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", prompt);
        messages.add(userMessage);

        requestBody.put("messages", messages);
        requestBody.put("max_tokens", 50);
        requestBody.put("temperature", 0.7);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();
        } else {
            throw new RuntimeException("OpenAI API call failed");
        }
    }

    private List<Long> parseRecommendedIds(String gptResponse) {
        try {
            // GPT 응답 전체에서 'content' 필드를 추출
            JsonNode root = objectMapper.readTree(gptResponse);
            String content = root.path("choices").get(0).path("message").path("content").asText();

            // 응답 문자열 정제: 코드 블록 (``` 및 json 태그) 제거
            content = content.trim();
            if (content.startsWith("```")) {
                int firstNewLine = content.indexOf("\n");
                if (firstNewLine != -1) {
                    content = content.substring(firstNewLine).trim();
                }
                if (content.endsWith("```")) {
                    content = content.substring(0, content.lastIndexOf("```")).trim();
                }
            }

            // 정제된 문자열은 이제 [5,6,8,15,12] 형식의 순수 JSON 배열이어야 함.
            JsonNode jsonArray = objectMapper.readTree(content);
            List<Long> result = new ArrayList<>();
            if (jsonArray.isArray()) {
                for (JsonNode jsonNode : jsonArray) {
                    result.add(jsonNode.asLong());
                }
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}

