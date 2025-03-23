package com.example.ssauc.user.bid.dto

import lombok.*
import java.time.LocalDateTime

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
class ProductInformDto {
    // ====================== 상품 정보 탭 ======================
    var name: String? = null // 상품 이름

    var tempPrice: Long? = null // 현재 입찰가

    var createdAt: LocalDateTime? = null // 등록 시간

    var endAt: LocalDateTime? = null // 마감 시간

    var totalTime: Long? = null // 경매 남은 시간 (초단위)

    var price: Long? = null // 즉시 구매가

    var imageUrl: String? = null // 사진 정보

    var bidCount: Int = 0 // 입찰 수

    var dealType: Int = 0 // 거래 방식

    var minIncrement: Int = 0 // 최소 입찰 단위

    // ====================== 판매자 정보 탭 ======================
    var userName: String? = null // 회원 이름

    var profileImage: String? = null // 프로필 사진

    var reputation: Double? = null // 평가 지표

    // ====================== 정보 설명 탭 ======================
    var description: String? = null // 상품 설명

    var location: String? = null // 지역

    var viewCount: Long? = null // 조회수

    var likeCount: Int = 0 // 좋아요 수
}
