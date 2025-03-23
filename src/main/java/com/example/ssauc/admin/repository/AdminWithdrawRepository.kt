package com.example.ssauc.admin.repository;

import com.example.ssauc.user.cash.entity.Withdraw;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminWithdrawRepository extends JpaRepository<Withdraw, Long> {
    Page<Withdraw> findByUser_UserNameContainingIgnoreCaseOrBankContainingIgnoreCaseOrAccountContainingIgnoreCase(
            String userName, String bank, String account, Pageable pageable);
}
