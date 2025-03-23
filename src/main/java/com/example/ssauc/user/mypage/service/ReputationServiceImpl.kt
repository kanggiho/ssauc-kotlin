package com.example.ssauc.user.mypage.service

import com.example.ssauc.common.service.CommonUserService
import com.example.ssauc.user.login.entity.Users
import com.example.ssauc.user.login.repository.UsersRepository
import com.example.ssauc.user.mypage.entity.ReputationHistory
import com.example.ssauc.user.mypage.entity.UserActivity
import com.example.ssauc.user.mypage.repository.ReputationHistoryRepository
import com.example.ssauc.user.mypage.repository.UserActivityRepository
import lombok.RequiredArgsConstructor
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import kotlin.math.max
import kotlin.math.min

@Service
@RequiredArgsConstructor
class ReputationServiceImpl : ReputationService {
    private val commonUserService: CommonUserService? = null
    private val userRepository: UsersRepository? = null
    private val reputationHistoryRepository: ReputationHistoryRepository? = null
    private val userActivityRepository: UserActivityRepository? = null

    override fun getCurrentUser(email: String?): Users {
        return commonUserService!!.getCurrentUser(email)
    }

    @Transactional
    override fun updateReputation(userId: Long, changeType: String, changeAmount: Double) {
        val user = getUserById(userId)

        // 기존 평판 업데이트
        val newReputation = applyReputationChange(user, changeAmount)

        // 평판 변경 이력 기록
        saveReputationHistory(user, changeType, changeAmount, newReputation)
    }

    @Transactional
    override fun updateReputationForOrder(userId: Long, changeType: String, changeAmount: Double) {
        val user = getUserById(userId)

        // 기존 평판 업데이트
        val newReputation = applyReputationChange(user, changeAmount)

        // 평판 변경 이력 기록
        saveReputationHistory(user, changeType, changeAmount, newReputation)

        // 거래 횟수 증가 및 월간 보너스 체크
        updateUserTradeCountAndBonus(user)
    }

    private fun getUserById(userId: Long): Users {
        return userRepository!!.findById(userId)
            .orElseThrow { IllegalArgumentException("User not found") }!!
    }

    // 기존 평판 업데이트
    private fun applyReputationChange(user: Users, changeAmount: Double): Double {
        var newReputation = user.reputation!! + changeAmount
        // 🌟 평판이 0점 미만이면 0점, 100점 초과이면 100점으로 제한
        newReputation = max(0.0, min(newReputation, 100.0))
        user.reputation = newReputation
        userRepository!!.save(user)
        return newReputation
    }

    // 평판 변경 이력 기록
    private fun saveReputationHistory(user: Users, changeType: String, changeAmount: Double, newReputation: Double) {
        val history = ReputationHistory.builder()
            .user(user)
            .changeType(changeType)
            .changeAmount(changeAmount)
            .newScore(newReputation)
            .createdAt(LocalDateTime.now())
            .build()
        // 거래 횟수 증가 및 월간 보너스 체크
        reputationHistoryRepository!!.save(history)
    }

    private fun updateUserTradeCountAndBonus(user: Users) {
        val activity = userActivityRepository!!.findByUser(user)
            .orElse(
                UserActivity.builder()
                    .user(user)
                    .monthlyTradeCount(0L)
                    .lastUpdated(LocalDateTime.now())
                    .build()
            )

        val newCount = activity.monthlyTradeCount + 1
        activity.monthlyTradeCount = newCount
        activity.lastUpdated = LocalDateTime.now()
        userActivityRepository.save(activity)

        // 월간 보너스 적용 (거래 횟수가 3이 되는 순간)
        if (newCount == 3L) {
            updateReputation(user.userId!!, "월간 거래 보너스", 3.0)
        }
    }
}
