package com.example.ssauc.admin.repository

import com.example.ssauc.user.cash.entity.Withdraw
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface AdminWithdrawRepository : JpaRepository<Withdraw?, Long?> {
    fun findByUser_UserNameContainingIgnoreCaseOrBankContainingIgnoreCaseOrAccountContainingIgnoreCase(
        userName: String?, bank: String?, account: String?, pageable: Pageable?
    ): Page<Withdraw?>?
}
