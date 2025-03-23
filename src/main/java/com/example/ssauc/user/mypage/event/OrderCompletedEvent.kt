package com.example.ssauc.user.mypage.event

import lombok.Getter
import org.springframework.context.ApplicationEvent
import java.time.LocalDateTime

@Getter
class OrderCompletedEvent(
    source: Any, // 거래(판매 및 구매) 완료 이벤트: reputation +1.0
    // 한 달에 거래 3회 이상 이벤트: reputation +3.0
    private val buyerId: Long, private val sellerId: Long, private val completedAt: LocalDateTime
) :
    ApplicationEvent(source)
