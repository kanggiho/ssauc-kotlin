package com.example.ssauc.user.cash.repository

import com.example.ssauc.user.cash.entity.Withdraw
import com.example.ssauc.user.login.entity.Users
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime

interface WithdrawRepository : JpaRepository<Withdraw?, Long?> {
    // 내역 확인용 메서드
    fun findByUser(user: Users?, pageable: Pageable?): Page<Withdraw?>?
    fun findByUserAndWithdrawAtBetween(
        user: Users?,
        start: LocalDateTime?,
        end: LocalDateTime?,
        pageable: Pageable?
    ): Page<Withdraw?>?

    // 현재 월 환급 신청 건수 확인용 메서드 추가
    fun countByUserAndRequestedAtBetween(user: Users?, start: LocalDateTime?, end: LocalDateTime?): Int

    // 기간 별 총 금액 계산
    // 기간 조회 x
    @Query("SELECT COALESCE(SUM(w.amount - w.commission), 0) FROM Withdraw w WHERE w.user = :user AND w.withdrawAt IS NOT NULL")
    fun sumNetAmountByUser(@Param("user") user: Users?): Long

    // 기간 조회 o
    @Query("SELECT COALESCE(SUM(w.amount - w.commission), 0) FROM Withdraw w WHERE w.user = :user AND w.withdrawAt BETWEEN :start AND :end")
    fun sumNetAmountByUserAndWithdrawAtBetween(
        @Param("user") user: Users?,
        @Param("start") start: LocalDateTime?,
        @Param("end") end: LocalDateTime?
    ): Long
}
