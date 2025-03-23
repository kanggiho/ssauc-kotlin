package com.example.ssauc.user.history.dto

import lombok.Builder
import lombok.Getter
import lombok.Setter
import java.time.LocalDateTime

@Getter
@Setter
@Builder
class SoldDetailDto {
    // Product 테이블 관련 필드
    public val productId: Long? = null
    public val productName: String? = null
    public val startPrice: Long? = null
    public val createdAt: LocalDateTime? = null
    public val dealType = 0
    public val imageUrl: String? = null

    // Orders 테이블 관련 필드
    public val orderId: Long? = null
    public val buyerName: String? = null
    public val totalPrice: Long? = null
    public val recipientName: String? = null
    public val recipientPhone: String? = null
    public val postalCode: String? = null
    public val deliveryAddress: String? = null
    public val deliveryStatus: String? = null
    public val orderDate: LocalDateTime? = null
    public val completedDate: LocalDateTime? = null
}

