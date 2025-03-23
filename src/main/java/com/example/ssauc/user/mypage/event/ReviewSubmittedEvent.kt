package com.example.ssauc.user.mypage.event

import lombok.Getter
import org.springframework.context.ApplicationEvent
import java.time.LocalDateTime

@Getter
class ReviewSubmittedEvent(
    source: Any, // 리뷰 작성자: reputation +0.5
    // 리뷰 대상자: reputation +baseScore(-1.5 ~ 1.5)
    private val reviewerId: Long?,
    private val revieweeId: Long?,
    private val baseScore: Double,
    private val createdAt: LocalDateTime
) :
    ApplicationEvent(source)
