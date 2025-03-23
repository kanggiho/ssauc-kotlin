package com.example.ssauc.user.product.dto

import lombok.AllArgsConstructor
import lombok.Builder
import lombok.Data
import lombok.NoArgsConstructor

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class ProductInsertDto {
    // 클라이언트에서 전달받은 카테고리 이름
    private val categoryName: String? = null

    // 상품 제목
    private val name: String? = null

    // 상품 설명
    private val description: String? = null

    // 즉시 구매가
    private val price: Long? = null

    // 현재 입찰가
    private val tempPrice: Long? = null

    // 경매 시작가
    private val startPrice: Long? = null

    // 첨부 이미지 파일명(향후 S3 연동 시 URL로 대체)
    private val imageUrl: String? = null

    // 마감 시간 관련 데이터
    private val auctionDate: String? = null // YYYY-MM-DD 형식
    private val auctionHour: Int? = null
    private val auctionMinute: Int? = null

    // 최소 입찰단위
    private val minIncrement = 0

    // 거래 유형 (0: 직거래, 1: 택배, 2: 둘 다 선택)
    private val dealType = 0
}
