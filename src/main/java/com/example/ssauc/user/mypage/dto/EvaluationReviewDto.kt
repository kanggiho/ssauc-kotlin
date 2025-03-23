package com.example.ssauc.user.mypage.dto

import lombok.*
import java.time.LocalDateTime

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
class EvaluationReviewDto {
    // 작성된 리뷰 리스트
    // 리뷰 상세 페이지 이동용 식별자
    private val reviewId: Long? = null

    // 해당 리뷰가 연결된 주문번호
    private val orderId: Long? = null

    // 리뷰 대상:
    // - written: 리뷰 작성 시 대상은 review.getReviewee()의 이름
    // - received: 리뷰 수신 시 대상은 review.getReviewer()의 이름
    private val reviewer: String? = null
    private val reviewee: String? = null
    private val profileImageUrl1: String? = null
    private val profileImageUrl2: String? = null

    // 주문한 상품의 이름 (orders → product)
    private val productId: Long? = null
    private val productName: String? = null
    private val productImageUrl: String? = null

    // 리뷰 작성일
    private val createdAt: LocalDateTime? = null
    private val transactionType: String? = null
}
