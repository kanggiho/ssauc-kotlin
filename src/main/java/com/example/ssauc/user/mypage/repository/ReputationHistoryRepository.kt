package com.example.ssauc.user.mypage.repository

import com.example.ssauc.user.login.entity.Users
import com.example.ssauc.user.mypage.entity.ReputationHistory
import org.springframework.data.jpa.repository.JpaRepository

interface ReputationHistoryRepository : JpaRepository<ReputationHistory?, Long?> {
    // 특정 사용자의 평판 기록이 있는지 확인하는 메서드 추가
    fun existsByUser(user: Users?): Boolean

    // 특정 사용자의 평판 변화 이력을 조회 (중복 계산 방지용)
    fun findByUserAndChangeTypeNotLike(user: Users?, pattern: String?): List<ReputationHistory?>?

    // 특정 사용자의 평판 기록 조회
    fun findByUser(user: Users?): List<ReputationHistory?>
}