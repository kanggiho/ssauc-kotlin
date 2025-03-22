package com.example.ssauc.user.mypage.repository;

import com.example.ssauc.user.login.entity.Users;
import com.example.ssauc.user.mypage.entity.UserActivity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserActivityRepository extends JpaRepository<UserActivity, Long> {
    Optional<UserActivity> findByUser(Users user);
}