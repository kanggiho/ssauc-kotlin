package com.example.ssauc.user.history.dto

import lombok.*
import java.time.LocalDateTime

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
class BuyBidHistoryDto {
    public val bidId: Long? = null // 입찰 ID
    public val productId: Long? = null // 상품 ID
    public val productName: String? = null // 상품 이름
    public val productImageUrl: String? = null
    public val sellerName: String? = null // 판매자 이름 (Product.seller)
    public val profileImageUrl: String? = null
    public val endAt: LocalDateTime? = null // 상품의 경매 마감 시간
    public val tempPrice: Long? = null // 현재가 (temp_price)
    public val bidPrice: Long? = null // 내 입찰가
    public val maxBidAmount: Long? = null // 자동입찰 한도
}
