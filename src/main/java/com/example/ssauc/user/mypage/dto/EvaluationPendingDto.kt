package com.example.ssauc.user.mypage.dto

import lombok.*
import java.time.LocalDateTime

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
class EvaluationPendingDto {
    // 작성할 리뷰 리스트
    // 해당 주문번호
    private val orderId: Long? = null

    // 리뷰 대상: 주문에서 로그인한 사용자가 buyer이면 seller의 이름, seller이면 buyer의 이름
    private val reviewTarget: String? = null
    private val profileImageUrl: String? = null

    // 주문한 상품 이름 (orders → product)
    private val productId: Long? = null
    private val productName: String? = null
    private val productImageUrl: String? = null

    // 주문 일자 (정렬용으로 활용 – 필요에 따라 추가 정보 표시 가능)
    private val orderDate: LocalDateTime? = null
    private val transactionType: String? = null
}
