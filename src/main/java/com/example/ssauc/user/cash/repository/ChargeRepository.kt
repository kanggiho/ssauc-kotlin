package com.example.ssauc.user.cash.repository

import com.example.ssauc.user.cash.entity.Charge
import com.example.ssauc.user.login.entity.Users
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime

interface ChargeRepository : JpaRepository<Charge?, Long?> {
    // 페이징 처리 메서드 추가
    fun findByUser(user: Users?, pageable: Pageable?): Page<Charge?>?
    fun findByUserAndCreatedAtBetween(
        user: Users?,
        start: LocalDateTime?,
        end: LocalDateTime?,
        pageable: Pageable?
    ): Page<Charge?>?

    // 기간 별 총 금액 계산
    // 기간 조회 x
    @Query("SELECT COALESCE(SUM(c.amount), 0) FROM Charge c WHERE c.user = :user AND c.status = '충전완료'")
    fun sumAmountByUser(@Param("user") user: Users?): Long

    // 기간 조회 o
    @Query("SELECT COALESCE(SUM(c.amount), 0) FROM Charge c WHERE c.user = :user AND c.createdAt BETWEEN :start AND :end AND c.status = '충전완료'")
    fun sumAmountByUserAndCreatedAtBetween(
        @Param("user") user: Users?,
        @Param("start") start: LocalDateTime?,
        @Param("end") end: LocalDateTime?
    ): Long
}