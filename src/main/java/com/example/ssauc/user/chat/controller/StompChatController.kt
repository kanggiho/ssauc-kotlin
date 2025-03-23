package com.example.ssauc.user.chat.controller

import com.example.ssauc.user.chat.dto.ChatMessageDto
import com.example.ssauc.user.chat.dto.ChatMessageResponse
import com.example.ssauc.user.chat.service.ChatService
import lombok.RequiredArgsConstructor
import lombok.extern.slf4j.Slf4j
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Controller

@Slf4j
@Controller
@RequiredArgsConstructor
class StompChatController {
    private val messagingTemplate: SimpMessagingTemplate? = null
    private val chatService: ChatService? = null

    /**
     * 클라이언트에서 /pub/chat/message 로 발행하면 이 메서드가 수신
     */
    @MessageMapping("/chat/message")
    fun message(messageDto: ChatMessageDto) {
        // DB 저장
        val savedMessage = chatService!!.saveMessage(
            messageDto.chatRoomId,
            messageDto.senderId,
            messageDto.otherUserId,
            messageDto.message
        )

        // 채팅방을 구독 중인 모든 세션에게 메시지 전송
        messagingTemplate!!.convertAndSend(
            "/sub/chat/room/" + savedMessage.chatRoom.chatRoomId,  // 프론트에 내려줄 DTO 형식
            ChatMessageResponse.of(savedMessage)
        )
    }
}
