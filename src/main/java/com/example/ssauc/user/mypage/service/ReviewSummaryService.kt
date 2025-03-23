package com.example.ssauc.user.mypage.service

import com.example.ssauc.user.login.repository.UsersRepository
import com.example.ssauc.user.pay.entity.Review
import com.example.ssauc.user.pay.repository.ReviewRepository
import lombok.RequiredArgsConstructor
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.*
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.util.*
import java.util.stream.Collectors

@Service
@RequiredArgsConstructor
class ReviewSummaryService {
    private val reviewRepository: ReviewRepository? = null
    private val usersRepository: UsersRepository? = null
    private val restTemplate: RestTemplate? = null // AppConfig class

    @Value("\${openai.api.key}")
    private val openaiApiKey: String? = null

    // 특정 유저의 리뷰가 5개 이상이면, 리뷰 내용을 모아서 GPT API를 통해 요약한 후,
    // Users 테이블의 reviewSummary 필드를 업데이트함.
    fun updateReviewSummaryForUser(userId: Long) {
        // 1. 해당 유저의 리뷰 수 확인
        val reviewCount = reviewRepository!!.countByReviewee_UserId(userId)

        if (reviewCount < 5) {
            // 리뷰가 5개 미만이면 요약하지 않음
            return
        }

        // 2. 최대 5개의 리뷰 내용 가져오기
        val reviews = reviewRepository.findTop5ByReviewee_UserIdOrderByCreatedAtDesc(userId)

        val comments = reviews.stream()
            .map { obj: Review -> obj.comment }
            .filter { obj: String? -> Objects.nonNull(obj) }
            .collect(Collectors.toList())
        if (comments.isEmpty()) {
            return
        }
        val joinedComments = java.lang.String.join("\n", comments)

        // 3. GPT에 보낼 프롬프트 구성 (예: 한국어 2-3문장 요약 요청)
        val prompt =
            """
             ':'뒤의 내용들은 중고 경매 플랫폼에서 한 유저에 대한 여려 유저들의 리뷰 내용이야, 해당 유저에 대한 리뷰를 한국어로 2-3문장으로 요약해줘.중복되는 표현은 하지 말고서로 모순되는 표현도 없었으면 좋겠어.한 명의 유저가 작성한 것처럼 자연스럽게 요약본을 만들어줘. :
             $joinedComments
             """.trimIndent()
        // 4. GPT API 호출 준비
        val requestBody: MutableMap<String, Any> = HashMap()
        requestBody["model"] = "gpt-3.5-turbo" // 또는 gpt-4-turbo, 사용 가능한 모델 선택
        val messages: MutableList<Map<String, String>> = ArrayList()
        messages.add(java.util.Map.of("role", "user", "content", prompt))
        requestBody["messages"] = messages

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.setBearerAuth(openaiApiKey!!)

        val entity = HttpEntity<Map<String, Any>>(requestBody, headers)

        // 5. GPT API 호출
        val response: ResponseEntity<Map<*, *>?> = restTemplate.postForEntity<Map<*, *>?>(
            "https://api.openai.com/v1/chat/completions",
            entity,
            MutableMap::class.java
        )

        var summary = ""
        if (response.statusCode === HttpStatus.OK && response.body != null) {
            try {
                // 응답에서 summary 추출 (choices 배열의 첫번째 요소)
                val choices = response.body!!["choices"] as List<Map<String, Any>>?
                if (choices != null && !choices.isEmpty()) {
                    val firstChoice = choices[0]
                    val message = firstChoice["message"] as Map<String, String>?
                    summary = message!!["content"]
                }
            } catch (e: Exception) {
                // 파싱 실패 시 로그 처리
                System.err.println("GPT 응답 파싱 실패: " + e.message)
                return
            }
        } else {
            System.err.println("GPT API 호출 실패: " + response.statusCode)
            return
        }

        // 6. Users 테이블에 요약 결과 저장
        val userOpt = usersRepository!!.findById(userId)
        if (userOpt.isPresent) {
            val user = userOpt.get()
            user.reviewSummary = summary
            usersRepository.save(user)
        }
    }
}
