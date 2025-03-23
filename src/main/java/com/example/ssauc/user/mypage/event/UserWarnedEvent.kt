package com.example.ssauc.user.mypage.event

import lombok.Getter
import org.springframework.context.ApplicationEvent

@Getter
class UserWarnedEvent(
    source: Any, // 유저 및 상품 신고에 대한 관리자의 경고 처리 이벤트: reputation -5.0
    private val reportedUserId: Long
) : ApplicationEvent(source)
