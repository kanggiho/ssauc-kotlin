package com.example.ssauc.user.bid.repository

import com.example.ssauc.user.bid.entity.ProductReport
import com.example.ssauc.user.login.entity.Users
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface ProductReportRepository : JpaRepository<ProductReport?, Long?> {
    fun findByReporter(reporter: Users?, pageable: Pageable?): Page<ProductReport?>?
}