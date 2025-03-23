package com.example.ssauc.user.chat.dto

import com.example.ssauc.user.chat.entity.ChatMessage
import lombok.AllArgsConstructor
import lombok.Getter
import lombok.NoArgsConstructor
import lombok.Setter
import java.time.LocalDateTime

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class ChatMessageResponse {
    private var messageId: Long? = null
    private var chatRoomId: Long? = null
    private var senderId: Long? = null
    private var message: String? = null
    private var sentAt: LocalDateTime? = null

    companion object {
        fun of(msg: ChatMessage): ChatMessageResponse {
            val dto = ChatMessageResponse()
            dto.messageId = msg.messageId
            dto.chatRoomId = msg.chatRoom.chatRoomId
            dto.senderId = msg.sender.userId
            dto.message = msg.message
            dto.sentAt = msg.sentAt
            return dto
        }
    }
}