package com.example.ssauc.user.login.repository

import com.example.ssauc.user.login.entity.Users
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

interface UsersRepository : JpaRepository<Users?, Long?> {
    fun findByEmail(email: String?): Optional<Users?>
    fun findByUserName(userName: String?): Optional<Users?>?
    fun findByPhone(phone: String?): Optional<Users?>?

    // active만 찾는 메서드 (예: findByUserNameAndStatus, findByEmailAndStatus)
    fun findByEmailAndStatus(email: String?, status: String?): Optional<Users?>?
    fun findByUserNameAndPhoneAndStatus(userName: String?, phone: String?, status: String?): Optional<Users?>?

    fun existsByEmail(email: String?): Boolean
    fun existsByUserName(userName: String?): Boolean
    fun existsByPhone(phone: String?): Boolean

    @Transactional
    @Modifying
    @Query("update Users u set u.cash = u.cash + ?1 where u.userId = ?2")
    fun addCash(cash: Long?, userId: Long?)

    @Transactional
    @Modifying
    @Query("update Users u set u.cash = u.cash - ?1 where u.userId = ?2")
    fun minusCash(cash: Long?, userId: Long?)

    @Transactional
    @Modifying
    @Query("UPDATE Users u SET u.warningCount = u.warningCount + :warningCount WHERE u.userId = :userId")
    fun updateUserByWarningCount(@Param("warningCount") warningCount: Int, @Param("userId") userId: Long?): Int

    fun findByLastLoginBefore(date: LocalDateTime?): List<Users?>?
}
