package com.example.ssauc.user.cash.repository;

import com.example.ssauc.user.cash.entity.Charge;
import com.example.ssauc.user.login.entity.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface ChargeRepository extends JpaRepository<Charge, Long> {
    // 페이징 처리 메서드 추가
    Page<Charge> findByUser(Users user, Pageable pageable);
    Page<Charge> findByUserAndCreatedAtBetween(Users user, LocalDateTime start, LocalDateTime end, Pageable pageable);

    // 기간 별 총 금액 계산
    // 기간 조회 x
    @Query("SELECT COALESCE(SUM(c.amount), 0) FROM Charge c WHERE c.user = :user AND c.status = '충전완료'")
    long sumAmountByUser(@Param("user") Users user);
    // 기간 조회 o
    @Query("SELECT COALESCE(SUM(c.amount), 0) FROM Charge c WHERE c.user = :user AND c.createdAt BETWEEN :start AND :end AND c.status = '충전완료'")
    long sumAmountByUserAndCreatedAtBetween(@Param("user") Users user,
                                            @Param("start") LocalDateTime start,
                                            @Param("end") LocalDateTime end);


}