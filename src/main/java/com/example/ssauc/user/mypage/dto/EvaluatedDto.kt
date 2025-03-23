package com.example.ssauc.user.mypage.dto

import lombok.AllArgsConstructor
import lombok.Builder
import lombok.Data
import lombok.NoArgsConstructor
import java.time.LocalDateTime

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class EvaluatedDto {
    private val reviewId: Long? = null
    private val orderId: Long? = null
    private val reviewerName: String? = null
    private val revieweeName: String? = null
    private val profileImageUrl: String? = null
    private val productId: Long? = null
    private val productName: String? = null
    private val productImageUrl: String? = null
    private val createdAt: LocalDateTime? = null
    private val transactionType: String? = null
    private val option1: Boolean? = null
    private val option2: Boolean? = null
    private val option3: Boolean? = null
    private val comment: String? = null
}
