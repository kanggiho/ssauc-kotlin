package com.example.ssauc.user.mypage.dto

import lombok.AllArgsConstructor
import lombok.Builder
import lombok.Data
import lombok.NoArgsConstructor

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class EvaluationDto {
    // 리뷰 작성
    private val orderId: Long? = null
    private val productId: Long? = null

    // 거래 유형은 프론트엔드에서 전달하지만 실제 DB에는 저장하지 않으므로, 주로 판단용으로 사용
    private val transactionType: String? = null

    // q1, q2, q3는 "positive" 또는 "negative" 문자열이 전달됨
    private val q1: String? = null
    private val q2: String? = null
    private val q3: String? = null

    // 사용자가 입력한 상세 후기 (최대 300자)
    private val reviewContent: String? = null

    // 프론트엔드에서 baseScore를 계산하여 전달 (예: 0.5 + 각 옵션에 따라 ±0.5)
    private val baseScore: Double? = null

    // 화면에 표시할 추가 정보
    private val productName: String? = null
    private val otherUserName: String? = null
}
