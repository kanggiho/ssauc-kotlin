package com.example.ssauc.user.recommendation.repository

import com.example.ssauc.user.product.entity.Product
import com.example.ssauc.user.recommendation.dto.RecommendationDto
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface RecommendRepository : JpaRepository<Product?, Long?> {
    @Query(
        ("SELECT new com.example.ssauc.user.recommendation.dto.RecommendationDto(" +
                "p.productId, p.name, p.description, p.price, u.location, p.imageUrl) " +
                "FROM Product p " +
                "JOIN p.seller u " +
                "WHERE p.endAt >= current_timestamp AND p.status = '판매중'")
    )
    fun findRecommendProductsWithoutLogin(): List<RecommendationDto?>? // 입찰마감, 판매 중인 상품만 가져온다.
}