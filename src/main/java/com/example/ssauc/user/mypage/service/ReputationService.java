package com.example.ssauc.user.mypage.service;

import com.example.ssauc.user.login.entity.Users;


public interface ReputationService {
    // 평판 관련 로직 담당

    Users getCurrentUser(String email);

    void updateReputation(Long userId, String changeType, double changeAmount);

    void updateReputationForOrder(Long userId, String changeType, double changeAmount);
}
