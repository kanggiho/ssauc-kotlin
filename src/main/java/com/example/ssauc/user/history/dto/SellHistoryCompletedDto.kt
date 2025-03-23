package com.example.ssauc.user.history.dto

import lombok.AllArgsConstructor
import lombok.Builder
import lombok.Data
import lombok.NoArgsConstructor
import java.time.LocalDateTime

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
class SellHistoryCompletedDto {
    public val orderId: Long? = null
    public val productId: Long? = null
    public val productName: String? = null
    public val productImageUrl: String? = null
    public val buyerName: String? = null
    public val profileImageUrl: String? = null
    public val totalPrice: Long? = null
    public val orderDate: LocalDateTime? = null
    public val completedDate: LocalDateTime? = null
}
