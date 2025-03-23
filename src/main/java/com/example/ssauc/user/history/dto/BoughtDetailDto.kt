package com.example.ssauc.user.history.dto

import lombok.*
import java.time.LocalDateTime

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
class BoughtDetailDto {
    // Product 테이블 정보
    public val productId: Long? = null
    public val productName: String? = null
    public val startPrice: Long? = null
    public val createdAt: LocalDateTime? = null
    public val dealType = 0
    public val imageUrl: String? = null

    // 판매자 정보 (Orders.seller 기준)
    public val sellerName: String? = null

    // Orders 테이블 정보
    public val orderId: Long? = null
    public val totalPrice: Long? = null
    public val recipientName: String? = null
    public val recipientPhone: String? = null
    public val postalCode: String? = null
    public val deliveryAddress: String? = null
    public val orderDate: LocalDateTime? = null
    public val completedDate: LocalDateTime? = null
    public val deliveryStatus: String? = null
}
