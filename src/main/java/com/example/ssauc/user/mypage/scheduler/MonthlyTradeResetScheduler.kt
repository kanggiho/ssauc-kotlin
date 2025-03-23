package com.example.ssauc.user.mypage.scheduler

import com.example.ssauc.user.mypage.repository.UserActivityRepository
import lombok.RequiredArgsConstructor
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
@RequiredArgsConstructor
class MonthlyTradeResetScheduler {
    // 월별 거래 횟수 초기화
    private val userActivityRepository: UserActivityRepository? = null

    // 매월 1일 00:00에 모든 유저의 monthly_trade_count를 초기화
    @Scheduled(cron = "0 0 8 1 * ?")
    fun resetMonthlyTradeCounts() {
        val activities = userActivityRepository!!.findAll()
        for (activity in activities) {
            activity.monthlyTradeCount = 0L
            activity.lastUpdated = LocalDateTime.now()
        }
        userActivityRepository.saveAll(activities)
    }
}
