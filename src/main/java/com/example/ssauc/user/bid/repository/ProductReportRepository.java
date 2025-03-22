package com.example.ssauc.user.bid.repository;

import com.example.ssauc.user.bid.entity.ProductReport;
import com.example.ssauc.user.login.entity.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductReportRepository extends JpaRepository<ProductReport, Long> {
    Page<ProductReport> findByReporter(Users reporter, Pageable pageable);
}