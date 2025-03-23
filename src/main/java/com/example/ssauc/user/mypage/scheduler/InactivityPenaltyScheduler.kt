package com.example.ssauc.user.mypage.scheduler

import com.example.ssauc.user.login.entity.Users
import com.example.ssauc.user.login.repository.UsersRepository
import com.example.ssauc.user.mypage.entity.ReputationHistory
import com.example.ssauc.user.mypage.repository.ReputationHistoryRepository
import lombok.RequiredArgsConstructor
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.math.max
import kotlin.math.min

@Component
@RequiredArgsConstructor
class InactivityPenaltyScheduler {
    // 장기 미접속 유저 reputation 감소
    private val usersRepository: UsersRepository? = null
    private val reputationHistoryRepository: ReputationHistoryRepository? = null

    // 매월 1일 00:00에 실행하도록 스케줄러 설정
    @Scheduled(cron = "0 0 9 1 * ?")
    fun applyInactivityPenalty() {
        val now = LocalDateTime.now()
        val threeMonthsAgo = now.minusMonths(3)

        // lastLogin이 3개월 이전인 사용자들 조회
        val inactiveUsers = usersRepository!!.findByLastLoginBefore(threeMonthsAgo)

        for (user in inactiveUsers!!) {
            // 미접속 개월 수 계산 (예: 5개월 미접속이면, penaltyMonths = 5 - 3 = 2)
            val monthsInactive = ChronoUnit.MONTHS.between(user.lastLogin, now)
            val penaltyMonths = max((monthsInactive - 3).toDouble(), 0.0).toLong()

            // 매월 -3%씩, 최대 -36%까지
            val penaltyRate = min(penaltyMonths * 0.03, 0.36)
            val penaltyAmount = user.reputation!! * penaltyRate
            val newReputation = user.reputation!! - penaltyAmount

            // 사용자 평판 업데이트
            user.reputation = newReputation
            usersRepository.save<Users>(user)

            // 평판 변경 이력 기록
            val history = ReputationHistory.builder()
                .user(user)
                .changeType("3개월 이상 미접속 패널티")
                .changeAmount(-penaltyAmount)
                .newScore(newReputation)
                .createdAt(now)
                .build()
            reputationHistoryRepository!!.save(history)
        }
    }
}
