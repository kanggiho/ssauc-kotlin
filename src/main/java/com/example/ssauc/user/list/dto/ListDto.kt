package com.example.ssauc.user.list.dto

import lombok.*
import java.time.LocalDateTime

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
class ListDto {
    private val productId: Long? = null // 다 Product 테이블
    private val imageUrl: String? = null
    private val name: String? = null
    private val price: Long? = null
    private val bidCount = 0
    private val endAt: LocalDateTime? = null
    private val createdAt: LocalDateTime? = null
    private val location: String? = null // Users 테이블
    private val likeCount = 0
    private val status: String? = null
}
