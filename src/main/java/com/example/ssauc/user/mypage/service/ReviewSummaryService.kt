package com.example.ssauc.user.mypage.service;

import com.example.ssauc.user.login.entity.Users;
import com.example.ssauc.user.login.repository.UsersRepository;
import com.example.ssauc.user.pay.entity.Review;
import com.example.ssauc.user.pay.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewSummaryService {

    private final ReviewRepository reviewRepository;
    private final UsersRepository usersRepository;
    private final RestTemplate restTemplate; // AppConfig class

    @Value("${openai.api.key}")
    private String openaiApiKey;

    // 특정 유저의 리뷰가 5개 이상이면, 리뷰 내용을 모아서 GPT API를 통해 요약한 후,
    // Users 테이블의 reviewSummary 필드를 업데이트함.
    public void updateReviewSummaryForUser(Long userId) {
        // 1. 해당 유저의 리뷰 수 확인
        Long reviewCount = reviewRepository.countByReviewee_UserId(userId);

        if (reviewCount < 5) {
            // 리뷰가 5개 미만이면 요약하지 않음
            return;
        }

        // 2. 최대 5개의 리뷰 내용 가져오기
        List<Review> reviews = reviewRepository.findTop5ByReviewee_UserIdOrderByCreatedAtDesc(userId);

        List<String> comments = reviews.stream()
                .map(Review::getComment)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (comments.isEmpty()) {
            return;
        }
        String joinedComments = String.join("\n", comments);

        // 3. GPT에 보낼 프롬프트 구성 (예: 한국어 2-3문장 요약 요청)
        String prompt = "':'뒤의 내용들은 중고 경매 플랫폼에서 한 유저에 대한 여려 유저들의 리뷰 내용이야, " +
                "해당 유저에 대한 리뷰를 한국어로 2-3문장으로 요약해줘." +
                "중복되는 표현은 하지 말고" +
                "서로 모순되는 표현도 없었으면 좋겠어." +
                "한 명의 유저가 작성한 것처럼 자연스럽게 요약본을 만들어줘. :\n" + joinedComments;
        // 4. GPT API 호출 준비
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-3.5-turbo"); // 또는 gpt-4-turbo, 사용 가능한 모델 선택
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "user", "content", prompt));
        requestBody.put("messages", messages);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openaiApiKey);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        // 5. GPT API 호출
        ResponseEntity<Map> response = restTemplate.postForEntity(
                "https://api.openai.com/v1/chat/completions",
                entity,
                Map.class
        );

        String summary = "";
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            try {
                // 응답에서 summary 추출 (choices 배열의 첫번째 요소)
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> firstChoice = choices.get(0);
                    Map<String, String> message = (Map<String, String>) firstChoice.get("message");
                    summary = message.get("content");
                }
            } catch (Exception e) {
                // 파싱 실패 시 로그 처리
                System.err.println("GPT 응답 파싱 실패: " + e.getMessage());
                return;
            }
        } else {
            System.err.println("GPT API 호출 실패: " + response.getStatusCode());
            return;
        }

        // 6. Users 테이블에 요약 결과 저장
        Optional<Users> userOpt = usersRepository.findById(userId);
        if (userOpt.isPresent()) {
            Users user = userOpt.get();
            user.setReviewSummary(summary);
            usersRepository.save(user);
        }
    }
}
