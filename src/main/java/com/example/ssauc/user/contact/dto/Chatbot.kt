package com.example.ssauc.user.contact.dto

import lombok.*

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
class Chatbot {
    public val sender: String? = null // "USER" or "BOT"
    public val message: String? = null // 메시지 내용
}