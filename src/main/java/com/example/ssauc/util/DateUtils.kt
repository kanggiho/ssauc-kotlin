package com.example.ssauc.util

import org.springframework.stereotype.Component
import java.time.Duration
import java.time.LocalDateTime

@Component("dateUtils") // Thymeleaf에서 #dateUtils로 호출 가능
class DateUtils {
    fun formatRemainingTime(endAt: LocalDateTime?): String {
        // 현재 시간과 종료 시간의 차이를 계산 (종료 시간이 미래라고 가정)
        val duration = Duration.between(LocalDateTime.now(), endAt)
        if (duration.isNegative) {
            return "종료됨"
        }
        val days = duration.toDays()
        val hours = duration.minusDays(days).toHours()
        return days.toString() + "일 " + hours + "시간"
    }
}
