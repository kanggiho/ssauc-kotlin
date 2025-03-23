package com.example.ssauc.user.mypage.event

import com.example.ssauc.user.mypage.service.ReputationService
import lombok.RequiredArgsConstructor
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import java.time.Duration

@Component
@RequiredArgsConstructor
class ReputationEventListener {
    // 이벤트 리스너
    private val reputationService: ReputationService? = null

    // 거래 완료 처리
    @EventListener
    fun handleOrderCompleted(event: OrderCompletedEvent) {
        reputationService!!.updateReputationForOrder(event.buyerId, "구매 완료", 1.0)
        reputationService.updateReputationForOrder(event.sellerId, "판매 완료", 1.0)
    }

    // 운송장 등록 이벤트 처리: 주문일로부터 24시간 이내라면 +1.0점
    @EventListener
    fun handleOrderShipped(event: OrderShippedEvent) {
        val diff = Duration.between(event.orderDate, event.shippedDate)
        if (diff.toHours() < 24) {
            reputationService!!.updateReputation(event.sellerId, "24시간 내 운송장 등록", 1.0)
        }
    }

    // 신고 경고 이벤트 처리: -5.0점 적용
    @EventListener
    fun handleUserWarned(event: UserWarnedEvent) {
        reputationService!!.updateReputation(event.reportedUserId, "신고 경고 처리", -5.0)
    }

    // 리뷰 등록 이벤트 처리:
    // 리뷰 작성자: reputation +0.5
    // 리뷰 대상자: reputation +baseScore(-1.5 ~ 1.5)
    @EventListener
    fun handleReviewSubmitted(event: ReviewSubmittedEvent) {
        // 리뷰어는 리뷰 작성 시 +0.5점
        reputationService!!.updateReputation(event.reviewerId, "리뷰 작성", 0.5)
        // 리뷰 대상은 받은 거래 후기의 baseScore 만큼 평판 변경 (긍정/부정 옵션에 따라 각각 +0.5 또는 -0.5)
        reputationService.updateReputation(event.revieweeId, "받은 거래 후기", event.baseScore)
    }
}
