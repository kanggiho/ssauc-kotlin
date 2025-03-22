package com.example.ssauc.user.bid.repository;

import com.example.ssauc.user.bid.entity.AutoBid;
import com.example.ssauc.user.login.entity.Users;
import com.example.ssauc.user.product.entity.Product;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.List;

public interface AutoBidRepository extends JpaRepository<AutoBid, Long> {
    // 특정 상품에 대해 active한 자동입찰 목록을 불러오는 쿼리

    List<AutoBid> findByProductAndActiveIsTrue(Product product);

    // 특정 사용자와 상품에 대해 활성화(active=true)된 AutoBid를 반환
    AutoBid findByUserAndProductAndActive(Users user, Product product, boolean active);

}