package com.example.ssauc.admin.repository;

import com.example.ssauc.user.chat.entity.Report;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

public interface AdminReportRepository extends JpaRepository<Report, Long> {
    @EntityGraph(attributePaths = {"reporter", "reportedUser"})
    Page<Report> findAll(Pageable pageable);

    @Transactional
    @Modifying
    @Query("UPDATE Report r SET r.status = :status , r.processedAt = :processedAt WHERE r.reportId = :reportId")
    int updateReportByReportId(@Param("status") String status, @Param("processedAt") LocalDateTime processedAt, @Param("reportId") Long reportId);
}