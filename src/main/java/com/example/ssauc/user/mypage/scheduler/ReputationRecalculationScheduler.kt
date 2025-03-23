package com.example.ssauc.user.mypage.scheduler

import com.example.ssauc.user.login.entity.Users
import com.example.ssauc.user.login.repository.UsersRepository
import com.example.ssauc.user.mypage.entity.ReputationHistory
import com.example.ssauc.user.mypage.repository.ReputationHistoryRepository
import lombok.RequiredArgsConstructor
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@Component
@RequiredArgsConstructor
class ReputationRecalculationScheduler {
    private val usersRepository: UsersRepository? = null
    private val reputationHistoryRepository: ReputationHistoryRepository? = null

    // 하루 1회
    @Scheduled(cron = "0 0 4 * * ?")
    fun recalculateReputations() {
        val now = LocalDateTime.now()

        // 모든 사용자 조회
        val users = usersRepository!!.findAll()

        for (user in users) {
            // 원본 평판 기록이 없다면 스킵
            if (!reputationHistoryRepository!!.existsByUser(user)) {
                continue
            }

            // "가중치 적용" 패턴이 포함되지 않은 평판 변화 내역 조회
            val histories =
                reputationHistoryRepository.findByUserAndChangeTypeNotLike(user, "%가중치 적용%")

            for (history in histories!!) {
                val days = ChronoUnit.DAYS.between(history.createdAt, now)

                // 3개월, 6개월, 12개월 조건에 맞춰 "단계별 추가 차감" 적용
                if (days >= 90 && days < 91) {
                    applyPartialReduction(user, history, BigDecimal("0.1"), "3개월 가중치 적용", now)
                } else if (days >= 180 && days < 181) {
                    applyPartialReduction(user, history, BigDecimal("0.2"), "6개월 가중치 적용", now)
                } else if (days >= 365 && days < 366) {
                    applyPartialReduction(user, history, BigDecimal("0.2"), "12개월 가중치 적용", now)
                }
            }
        }
    }

    // 기간 별 차감 정도 계산
    // @param fraction: 원본 점수 대비 차감 비율 (예: 0.1, 0.2)
    private fun applyPartialReduction(
        user: Users, history: ReputationHistory,
        fraction: BigDecimal, changeType: String, now: LocalDateTime
    ) {
        // 원본 점수를 BigDecimal로 변환 후 소숫점 1자리 반올림
        val original = BigDecimal.valueOf(history.changeAmount)
            .setScale(1, RoundingMode.HALF_UP)

        // 차감할 양: 원본 * fraction, 소숫점 1자리 반올림
        val reductionBD = original.multiply(fraction)
            .setScale(1, RoundingMode.HALF_UP)

        // 현재 평판을 BigDecimal로 변환 후 소숫점 1자리 반올림
        val currentReputation = BigDecimal.valueOf(user.reputation!!)
            .setScale(1, RoundingMode.HALF_UP)

        // 새 평판 = 현재 평판 - 차감량, 음수면 0으로 처리하고 반올림
        val newReputation = currentReputation.subtract(reductionBD)
            .max(BigDecimal.ZERO)
            .setScale(1, RoundingMode.HALF_UP)

        // 사용자 평판 업데이트 (DB 필드가 double인 경우 doubleValue() 사용)
        user.reputation = newReputation.toDouble()
        usersRepository!!.save(user)


        // 적용된 가중치 대상 history_id를 changeType 앞에 추가
        val updatedChangeType = history.historyId.toString() + " - " + changeType

        // 감소량이 있을 경우 기록
        if (reductionBD.compareTo(BigDecimal.ZERO) > 0) {
            val partialRecord = ReputationHistory.builder()
                .user(user)
                .changeType(updatedChangeType) // 원본과 구분하기 위해 고정
                .changeAmount(-reductionBD.toDouble())
                .newScore(newReputation.toDouble())
                .createdAt(now)
                .build()
            reputationHistoryRepository!!.save(partialRecord)
        }
    }
}
