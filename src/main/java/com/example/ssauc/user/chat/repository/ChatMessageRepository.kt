package com.example.ssauc.user.chat.repository

import com.example.ssauc.user.chat.entity.ChatMessage
import com.example.ssauc.user.chat.entity.ChatRoom
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ChatMessageRepository : JpaRepository<ChatMessage?, Long?> {
    fun findByChatRoom(chatRoom: ChatRoom?): List<ChatMessage?>?

    // 채팅방 메시지 목록 시간순 조회
    fun findByChatRoomChatRoomIdOrderBySentAtAsc(chatRoomId: Long?): List<ChatMessage?>?
}