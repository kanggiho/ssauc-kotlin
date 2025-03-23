package com.example.ssauc.user.mypage.event

import com.example.ssauc.user.mypage.service.ReviewSummaryService
import lombok.RequiredArgsConstructor
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
@RequiredArgsConstructor
class ReviewSummaryEventListener {
    private val reviewSummaryService: ReviewSummaryService? = null

    @EventListener
    fun handleReviewSubmittedEvent(event: ReviewSubmittedEvent) {
        // 리뷰 제출 이벤트에서 revieweeId를 추출하여 평판 요약 업데이트 호출
        val revieweeId = event.revieweeId
        reviewSummaryService!!.updateReviewSummaryForUser(revieweeId)
    }
}
