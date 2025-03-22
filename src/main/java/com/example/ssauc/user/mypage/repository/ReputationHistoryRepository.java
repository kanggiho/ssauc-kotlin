package com.example.ssauc.user.mypage.repository;

import com.example.ssauc.user.login.entity.Users;
import com.example.ssauc.user.mypage.entity.ReputationHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReputationHistoryRepository extends JpaRepository<ReputationHistory, Long> {
    // 특정 사용자의 평판 기록이 있는지 확인하는 메서드 추가
    boolean existsByUser(Users user);

    // 특정 사용자의 평판 변화 이력을 조회 (중복 계산 방지용)
    List<ReputationHistory> findByUserAndChangeTypeNotLike(Users user, String pattern);

    // 특정 사용자의 평판 기록 조회
    List<ReputationHistory> findByUser(Users user);
}