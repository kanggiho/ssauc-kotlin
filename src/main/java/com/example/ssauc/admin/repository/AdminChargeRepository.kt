package com.example.ssauc.admin.repository;

import com.example.ssauc.user.cash.entity.Charge;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminChargeRepository extends JpaRepository<Charge, Long> {
    Page<Charge> findByUser_UserNameContainingIgnoreCaseOrChargeTypeContainingIgnoreCase(String userName, String chargeType, Pageable pageable);
}
