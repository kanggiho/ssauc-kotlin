package com.example.ssauc.user.contact.controller;

import com.example.ssauc.user.chat.dto.ChatMessageDto;
import com.example.ssauc.user.contact.dto.Chatbot;
import com.example.ssauc.user.contact.service.ChatbotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class StompChatbotController {

    private final ChatbotService chatbotService;

    /**
     * 사용자로부터 STOMP 메시지(/app/chat.message)가 오면,
     * ChatbotService를 통해 OpenAI API 호출 후,
     * /topic/chatbot 으로 BOT 응답을 보낸다.
     */
    @MessageMapping("/chat.message")     // 클라이언트가 /app/chat.message 로 보냄
    @SendTo("/topic/chatbot")           // 모두가 구독한 /topic/chatbot 으로 응답
    public Chatbot processMessage(Chatbot userMessage) {
        // 사용자가 보낸 메시지 추출
        String message = userMessage.getMessage();

        // ChatbotService를 이용하여 ChatGPT 응답 생성
        Chatbot response = chatbotService.processUserMessage(message);

        // ChatGPT 응답을 반환하면, 구독자("/topic/chatbot")들에게 실시간 전송됨
        return response;
    }

}