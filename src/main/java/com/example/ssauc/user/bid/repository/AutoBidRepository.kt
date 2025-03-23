package com.example.ssauc.user.bid.repository

import com.example.ssauc.user.bid.entity.AutoBid
import com.example.ssauc.user.login.entity.Users
import com.example.ssauc.user.product.entity.Product
import org.springframework.data.jpa.repository.JpaRepository

interface AutoBidRepository : JpaRepository<AutoBid?, Long?> {
    // 특정 상품에 대해 active한 자동입찰 목록을 불러오는 쿼리
    fun findByProductAndActiveIsTrue(product: Product?): List<AutoBid?>?

    // 특정 사용자와 상품에 대해 활성화(active=true)된 AutoBid를 반환
    fun findByUserAndProductAndActive(user: Users?, product: Product?, active: Boolean): AutoBid?
}