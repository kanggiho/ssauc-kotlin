package com.example.ssauc.user.contact.service;

import com.example.ssauc.user.contact.dto.Chatbot;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatbotService {

    // application.yml 등에서 OPENAI_API_KEY를 주입받는다고 가정
    @Value("${openai.api.key}")
    private String openaiApiKey;

    /**
     * 사용자 메시지를 ChatGPT API로 보내고, 결과를 받아서 반환
     */
    public Chatbot processUserMessage(String userMessage) {

        // 1) OpenAiService 생성 (API 키로 인증)
        OpenAiService openAiService = new OpenAiService(openaiApiKey);

        // 2) ChatGPT에게 보낼 메시지 구성
        // - role("user"), content("사용자 메시지")
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new ChatMessage("user", userMessage));

        // 3) ChatCompletionRequest 생성
        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model("gpt-3.5-turbo")  // 모델명 (gpt-3.5-turbo, gpt-4 등)
                .messages(messages)
                .maxTokens(800)         // 응답 토큰 제한 (적절히 조절)
                .temperature(0.8)       // 창의성 (0 ~ 1)
                .build();

        // 4) ChatGPT API 호출
        ChatCompletionResult result = openAiService.createChatCompletion(request);

        if (result.getChoices() == null || result.getChoices().isEmpty()) {
            // 응답이 없을 경우 예외 처리 or 기본 메시지
            return Chatbot.builder()
                    .sender("BOT")
                    .message("죄송합니다. 답변을 생성할 수 없었습니다.")
                    .build();
        }

        // 5) 응답 메시지 추출 (여러 choices 중 첫 번째)
        String reply = result.getChoices().get(0).getMessage().getContent();

        // 6) Chatbot 객체 생성하여 반환 ("BOT"으로 가정)
        return Chatbot.builder()
                .sender("BOT")
                .message(reply.trim())
                .build();
    }
}