package com.example.ssauc.user.product.dto

import lombok.AllArgsConstructor
import lombok.Builder
import lombok.Data
import lombok.NoArgsConstructor

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class ProductUpdateDto {
    private val productId: Long? = null
    private val categoryName: String? = null
    private val name: String? = null
    private val description: String? = null
    private val price: Long? = null
    private val startPrice: Long? = null
    private val imageUrl: String? = null
    private val auctionDate: String? = null
    private val auctionHour: Int? = null
    private val auctionMinute: Int? = null
    private val minIncrement = 0
    private val dealType = 0
}
