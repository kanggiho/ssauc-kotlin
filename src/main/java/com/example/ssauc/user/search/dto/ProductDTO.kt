package com.example.ssauc.user.search.dto

import lombok.AllArgsConstructor
import lombok.Getter
import lombok.Setter
import lombok.ToString

@Getter
@Setter
@AllArgsConstructor
@ToString
class ProductDTO {
    private val productId: Long? = null // 상품 ID
    private val name: String? = null // 상품명
    private val description: String? = null // 상품 설명
    private val categoryName: String? = null // 카테고리명
    private val price: Long? = null // 즉시 구매가
    private val startPrice: Long? = null // 경매 시작가
    private val tempPrice: Long? = null // 현재 입찰가 (입찰 중인 경우)
    private val minIncrement = 0 // 최소 입찰 증가 단위
    private val bidCount = 0 // 입찰 수
    private val likeCount = 0 // 좋아요 개수
    private val imageUrl: String? = null // 상품 이미지 URL
    private val viewCount: Long? = null // 조회수
    private val status: String? = null // 판매 상태 (판매 중, 판매 완료 등)
    private val location: String? = null // 지역
}
