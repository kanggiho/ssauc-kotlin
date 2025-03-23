package com.example.ssauc.user.chat.repository

import com.example.ssauc.user.chat.entity.Report
import com.example.ssauc.user.login.entity.Users
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ReportRepository : JpaRepository<Report?, Long?> {
    fun findByReporter(reporter: Users?, pageable: Pageable?): Page<Report?>?
}