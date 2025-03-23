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
class SellHistoryOngoingDto {
    public val productId: Long? = null
    public val productName: String? = null
    public val productImageUrl: String? = null
    public val tempPrice: Long? = null
    public val price: Long? = null
    public val createdAt: LocalDateTime? = null
    public val endAt: LocalDateTime? = null
}
