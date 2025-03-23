package com.example.ssauc.user.recommendation.service

import com.example.ssauc.user.recommendation.dto.RecommendationDto
import com.example.ssauc.user.recommendation.repository.RecommendRepository
import com.fasterxml.jackson.databind.ObjectMapper
import lombok.extern.slf4j.Slf4j
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.util.stream.Collectors

@Service
@Slf4j
class RecommendationService(
    private val recommendRepository: RecommendRepository,
    private val objectMapper: ObjectMapper
) {
    private val restTemplate = RestTemplate()

    @Value("\${openai.api.key}")
    private val openaiApiKey: String? = null

    /**
     * 현재 상품과 비슷한 상품 추천 메소드
     * @param currentProductId 현재 상세 페이지에서 보고 있는 상품 ID
     * @return 현재 상품과 비슷한 상품 리스트
     */
    fun getSimilarProducts(currentProductId: Long): List<RecommendationDto?> {
        // 전체 후보 상품 조회 (필요하다면 조건 추가)
        val products = recommendRepository.findRecommendProductsWithoutLogin()

        // 현재 상품 정보 추출
        val currentProduct = products!!.stream()
            .filter { p: RecommendationDto? -> p.getProductId() == currentProductId }
            .findFirst()
            .orElse(null)
        if (currentProduct == null) {
            return emptyList<RecommendationDto>()
        }

        // 현재 상품은 제외
        val candidateProducts = products.stream()
            .filter { p: RecommendationDto? -> p.getProductId() != currentProductId }
            .collect(Collectors.toList())

        // 현재 상품 정보를 별도로 기재 (원하는 경우, productRepository에서 조회할 수 있음)
        // 여기서는 간단히 currentProductId만 활용
        val prompt = buildSimilarPrompt(candidateProducts, currentProduct)

        val gptResponse = callGptApi(prompt)

        val similarIds = parseRecommendedIds(gptResponse)

        // GPT에서 추천한 ID와 일치하는 상품만 필터링
        return candidateProducts.stream()
            .filter { p: RecommendationDto? -> similarIds.contains(p.getProductId()) }
            .collect(Collectors.toList())
    }

    /**
     * 비슷한 상품 추천을 위한 프롬프트 구성
     * @param products 후보 상품 목록
     * @return GPT에 전달할 프롬프트 문자열
     */
    private fun buildSimilarPrompt(products: List<RecommendationDto>, currentProduct: RecommendationDto): String {
        val sb = StringBuilder()
        sb.append("현재 상품 정보:\n")
        sb.append("상품명: ").append(currentProduct.name).append("\n")
        sb.append("설명: ").append(currentProduct.description).append("\n\n")
        sb.append("후보 상품 목록 중에서 현재 상품의 제목과 설명이 가장 유사한 상위 5개 상품의 제품 ID만을 오직 JSON 배열 형식으로 출력해 주세요.\n")
        sb.append("출력 예: [1,3,5,8,11]. 추가 텍스트나 설명은 전혀 포함하지 말아 주세요.\n")
        sb.append("후보 상품 목록 (각 행은 '제품ID, 상품명, 설명' 형식):\n")
        for (product in products) {
            sb.append(product.productId)
                .append(", ")
                .append(product.name)
                .append(", ")
                .append(product.description)
                .append("\n")
        }

        return sb.toString()
    }

    private fun callGptApi(prompt: String): String? {
        val url = "https://api.openai.com/v1/chat/completions"
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers["Authorization"] = "Bearer $openaiApiKey"

        val requestBody: MutableMap<String, Any> = HashMap()
        requestBody["model"] = "gpt-3.5-turbo"

        val messages: MutableList<Map<String, String>> = ArrayList()
        val systemMessage: MutableMap<String, String> = HashMap()
        systemMessage["role"] = "system"
        systemMessage["content"] = "너는 상품 추천 전문가야."
        messages.add(systemMessage)

        val userMessage: MutableMap<String, String> = HashMap()
        userMessage["role"] = "user"
        userMessage["content"] = prompt
        messages.add(userMessage)

        requestBody["messages"] = messages
        requestBody["max_tokens"] = 50
        requestBody["temperature"] = 0.7

        val entity = HttpEntity<Map<String, Any>>(requestBody, headers)
        val response = restTemplate.postForEntity(url, entity, String::class.java)

        if (response.statusCode === HttpStatus.OK) {
            return response.body
        } else {
            throw RuntimeException("OpenAI API call failed")
        }
    }

    private fun parseRecommendedIds(gptResponse: String?): List<Long> {
        try {
            // GPT 응답 전체에서 'content' 필드를 추출
            val root = objectMapper.readTree(gptResponse)
            var content = root.path("choices")[0].path("message").path("content").asText()

            // 응답 문자열 정제: 코드 블록 (``` 및 json 태그) 제거
            content = content.trim { it <= ' ' }
            if (content.startsWith("```")) {
                val firstNewLine = content.indexOf("\n")
                if (firstNewLine != -1) {
                    content = content.substring(firstNewLine).trim { it <= ' ' }
                }
                if (content.endsWith("```")) {
                    content = content.substring(0, content.lastIndexOf("```")).trim { it <= ' ' }
                }
            }

            // 정제된 문자열은 이제 [5,6,8,15,12] 형식의 순수 JSON 배열이어야 함.
            val jsonArray = objectMapper.readTree(content)
            val result: MutableList<Long> = ArrayList()
            if (jsonArray.isArray) {
                for (jsonNode in jsonArray) {
                    result.add(jsonNode.asLong())
                }
            }
            return result
        } catch (e: Exception) {
            e.printStackTrace()
            return emptyList()
        }
    }
}

