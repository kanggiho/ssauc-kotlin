package com.example.ssauc.user.chat.dto

import lombok.*

@Getter
@Setter
@Data
@AllArgsConstructor
@NoArgsConstructor
class ChatMessageDto {
    private val chatRoomId: Long? = null
    private val senderId: Long? = null
    private val otherUserId: Long? = null
    private val message: String? = null
}