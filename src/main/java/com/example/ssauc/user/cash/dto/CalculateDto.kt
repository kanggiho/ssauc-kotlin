package com.example.ssauc.user.cash.dto

import lombok.*
import java.time.LocalDateTime

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
class CalculateDto {
    private val orderId: Long? = null
    private val paymentAmount: Long? = null
    private val productId: Long? = null
    private val productName: String? = null // orders에서 가져온 상품 이름
    private val productImageUrl: String? = null
    private val paymentTime: LocalDateTime? = null // 판매자: orders.completedDate, 구매자: payment.paymentDate
    private val orderStatus: String? = null // orders.order_status
}
