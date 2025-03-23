package com.example.ssauc.user.mypage.service

import com.example.ssauc.user.login.entity.Users

interface ReputationService {
    // 평판 관련 로직 담당
    fun getCurrentUser(email: String?): Users

    fun updateReputation(userId: Long, changeType: String, changeAmount: Double)

    fun updateReputationForOrder(userId: Long, changeType: String, changeAmount: Double)
}
