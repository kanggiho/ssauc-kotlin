package com.example.ssauc.admin.repository

import com.example.ssauc.user.chat.entity.Report
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

interface AdminReportRepository : JpaRepository<Report?, Long?> {
    @EntityGraph(attributePaths = ["reporter", "reportedUser"])
    override fun findAll(pageable: Pageable): Page<Report?>

    @Transactional
    @Modifying
    @Query("UPDATE Report r SET r.status = :status , r.processedAt = :processedAt WHERE r.reportId = :reportId")
    fun updateReportByReportId(
        @Param("status") status: String?,
        @Param("processedAt") processedAt: LocalDateTime?,
        @Param("reportId") reportId: Long?
    ): Int
}