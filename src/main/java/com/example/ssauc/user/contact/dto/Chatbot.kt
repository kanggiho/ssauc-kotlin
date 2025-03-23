package com.example.ssauc.user.contact.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Chatbot {
    private String sender;   // "USER" or "BOT"
    private String message;  // 메시지 내용
}