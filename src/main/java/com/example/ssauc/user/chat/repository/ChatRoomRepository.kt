package com.example.ssauc.user.chat.repository

import com.example.ssauc.user.chat.entity.ChatRoom
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface ChatRoomRepository : JpaRepository<ChatRoom?, Long?> {
    // 특정 userId가 buyer이거나, product의 seller가 userId인 모든 방
    fun findByBuyerUserIdOrProductSellerUserId(buyerId: Long?, sellerId: Long?): List<ChatRoom?>?
    fun findByProductProductIdAndBuyerUserId(productId: Long?, buyerId: Long?): Optional<ChatRoom?>?
}