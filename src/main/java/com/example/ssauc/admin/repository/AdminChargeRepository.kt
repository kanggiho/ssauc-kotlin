package com.example.ssauc.admin.repository

import com.example.ssauc.user.cash.entity.Charge
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface AdminChargeRepository : JpaRepository<Charge?, Long?> {
    fun findByUser_UserNameContainingIgnoreCaseOrChargeTypeContainingIgnoreCase(
        userName: String?,
        chargeType: String?,
        pageable: Pageable?
    ): Page<Charge?>?
}
