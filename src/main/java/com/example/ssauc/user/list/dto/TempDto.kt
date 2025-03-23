package com.example.ssauc.user.list.dto

import lombok.*

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
class TempDto {
    private val productId: Long? = null // 다 Product 테이블
    private val imageUrl: String? = null
    private val name: String? = null
    private val price: String? = null
    private val bidCount: String? = null
    private val gap: String? = null
    private val location: String? = null // Users 테이블
    private val likeCount: String? = null
    private val liked = false
    private val status: String? = null
}
