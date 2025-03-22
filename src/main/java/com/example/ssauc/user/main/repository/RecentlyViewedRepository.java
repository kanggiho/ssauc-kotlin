package com.example.ssauc.user.main.repository;

import com.example.ssauc.user.login.entity.Users;
import com.example.ssauc.user.main.entity.RecentlyViewed;
import com.example.ssauc.user.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecentlyViewedRepository extends JpaRepository<RecentlyViewed, Long> {
    // 특정 유저에 대해 최근 본 상품을 날짜(시간) 내림차순으로 조회
    List<RecentlyViewed> findAllByUserOrderByViewedAtDesc(Users user);

    // 특정 유저와 특정 상품으로 이미 본 기록이 있는지 확인
    RecentlyViewed findByUserAndProduct(Users user, Product product);
}