package com.example.ssauc.user.chat.dto;

import com.example.ssauc.user.chat.entity.ChatMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageResponse {
    private Long messageId;
    private Long chatRoomId;
    private Long senderId;
    private String message;
    private LocalDateTime sentAt;

    public static ChatMessageResponse of(ChatMessage msg) {
        ChatMessageResponse dto = new ChatMessageResponse();
        dto.messageId = msg.getMessageId();
        dto.chatRoomId = msg.getChatRoom().getChatRoomId();
        dto.senderId = msg.getSender().getUserId();
        dto.message = msg.getMessage();
        dto.sentAt = msg.getSentAt();
        return dto;
    }
}