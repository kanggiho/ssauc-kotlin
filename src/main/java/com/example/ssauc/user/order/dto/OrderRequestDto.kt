package com.example.ssauc.user.order.dto

import lombok.*

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
class OrderRequestDto {
    private val productId: Long? = null
    private val buyerId: Long? = null
    private val sellerId: Long? = null
    private val totalPayment = 0
    private val postalCode: String? = null
    private val deliveryAddress: String? = null
    private val selectedOption: String? = null
}
