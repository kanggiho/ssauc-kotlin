package com.example.ssauc.user.chat.repository;

import com.example.ssauc.user.chat.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    // 특정 userId가 buyer이거나, product의 seller가 userId인 모든 방
    List<ChatRoom> findByBuyerUserIdOrProductSellerUserId(Long buyerId, Long sellerId);
    Optional<ChatRoom> findByProductProductIdAndBuyerUserId(Long productId, Long buyerId);
}