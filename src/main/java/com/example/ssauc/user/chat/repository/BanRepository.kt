package com.example.ssauc.user.chat.repository

import com.example.ssauc.user.chat.entity.Ban
import com.example.ssauc.user.login.entity.Users
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface BanRepository : JpaRepository<Ban?, Long?> {
    // 로그인한 사용자의 차단 내역 조회 (Users 엔티티의 userId 필드를 기준) (한 페이지에 10개씩)
    fun findByUserUserId(userId: Long?, pageable: Pageable?): Page<Ban?>?

    // 특정 회원이 차단한 목록(내가 차단한 사용자목록 조회)
    fun findAllByUser(user: Users?): List<Ban?>?

    // 특정 유저가 특정 유저를 차단했는지 조회
    fun findByUserAndBlockedUser(user: Users?, blockedUser: Users?): Optional<Ban?>?


    fun existsByUserAndBlockedUserAndStatus(user: Users?, blockedUser: Users?, status: Int): Boolean

    fun findByUserAndBlockedUserAndStatus(user: Users?, blockedUser: Users?, status: Int): Optional<Ban?>?
}