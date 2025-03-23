package com.example.ssauc.user.mypage.event

import lombok.Getter
import org.springframework.context.ApplicationEvent
import java.time.LocalDateTime

@Getter
class OrderShippedEvent(
    source: Any, // 운송장 정보 등록 이벤트(주문 후 24시간 이내): reputation +1.0
    private val sellerId: Long, private val orderDate: LocalDateTime, private val shippedDate: LocalDateTime
) :
    ApplicationEvent(source)
