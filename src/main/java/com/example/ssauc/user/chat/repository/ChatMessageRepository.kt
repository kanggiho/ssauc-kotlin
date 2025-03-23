package com.example.ssauc.user.chat.repository;

import com.example.ssauc.user.chat.entity.ChatMessage;
import com.example.ssauc.user.chat.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByChatRoom(ChatRoom chatRoom);
    // 채팅방 메시지 목록 시간순 조회
    List<ChatMessage> findByChatRoomChatRoomIdOrderBySentAtAsc(Long chatRoomId);
}