package com.example.ssauc.admin.repository

import com.example.ssauc.user.login.entity.Users
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.transaction.annotation.Transactional

interface AdminUserRepository : JpaRepository<Users?, Long?> {
    override fun findAll(pageable: Pageable): Page<Users?>

    @Transactional
    @Modifying
    @Query("UPDATE Users u SET u.status = :status WHERE u.userId = :userId")
    fun updateUsersByUserId(@Param("status") status: String?, @Param("userId") userId: Long?): Int


    fun findByWarningCountGreaterThanEqual(warningCount: Int): List<Users?>?

    fun findByUserNameContainingIgnoreCase(keyword: String?, pageable: Pageable?): Page<Users?>?
}