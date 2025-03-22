package com.example.ssauc.user.chat.repository;

import com.example.ssauc.user.chat.entity.Ban;
import com.example.ssauc.user.login.entity.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BanRepository extends JpaRepository<Ban, Long> {
    // 로그인한 사용자의 차단 내역 조회 (Users 엔티티의 userId 필드를 기준) (한 페이지에 10개씩)
    Page<Ban> findByUserUserId(Long userId, Pageable pageable);

    // 특정 회원이 차단한 목록(내가 차단한 사용자목록 조회)
    List<Ban> findAllByUser(Users user);

    // 특정 유저가 특정 유저를 차단했는지 조회
    Optional<Ban> findByUserAndBlockedUser(Users user, Users blockedUser);


    boolean existsByUserAndBlockedUserAndStatus(Users user, Users blockedUser, int status);

    Optional<Ban> findByUserAndBlockedUserAndStatus(Users user, Users blockedUser, int status);
}