package com.example.ssauc.user.history.dto

import lombok.*
import java.time.LocalDateTime

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
class BuyHistoryDto {
    public val orderId: Long? = null
    public val productId: Long? = null
    public val productName: String? = null
    public val productImageUrl: String? = null
    public val sellerName: String? = null
    public val profileImageUrl: String? = null
    public val totalPrice: Long? = null
    public val orderDate: LocalDateTime? = null
    public val completedDate: LocalDateTime? = null
}
