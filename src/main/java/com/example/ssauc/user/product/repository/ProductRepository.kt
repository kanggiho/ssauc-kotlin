package com.example.ssauc.user.product.repository

import com.example.ssauc.user.login.entity.Users
import com.example.ssauc.user.product.entity.Product
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

interface ProductRepository : JpaRepository<Product?, Long?> {
    @Transactional
    @Modifying
    @Query("update Product p set p.status = '판매완료' where p.productId = :productId")
    fun completeSell(@Param("productId") productId: Long?)

    fun findBySellerAndStatus(seller: Users?, status: String?, pageable: Pageable?): Page<Product?>?

    // 판매중 상태의 상품 중, 경매 마감 시간이 cutoff 이상인 항목 (판매중 리스트)
    fun findBySellerAndStatusAndEndAtGreaterThanEqual(
        seller: Users?,
        status: String?,
        cutoff: LocalDateTime?,
        pageable: Pageable?
    ): Page<Product?>?

    // 판매중 상태의 상품 중, 경매 마감 시간이 cutoff 미만인 항목 (판매 마감 리스트)
    fun findBySellerAndStatusAndEndAtBefore(
        seller: Users?,
        status: String?,
        cutoff: LocalDateTime?,
        pageable: Pageable?
    ): Page<Product?>?


    @Query(
        ("SELECT p FROM Product p " +
                "JOIN FETCH p.category " +
                "JOIN FETCH p.seller " +
                "WHERE ( :keyword IS NULL OR p.name LIKE %:keyword% ) " +
                "AND ( :auctionOnly IS NULL OR (p.status = '판매중' AND p.tempPrice IS NOT NULL) ) " +
                "AND ( :categories IS NULL OR p.category.name IN :categories ) " +
                "AND ( :minPrice IS NULL OR p.price >= :minPrice ) " +
                "AND ( :maxPrice IS NULL OR p.price <= :maxPrice )")
    )
    fun findByFilters(
        @Param("keyword") keyword: String?,
        @Param("auctionOnly") auctionOnly: Boolean?,
        @Param("categories") categories: List<String?>?,
        @Param("minPrice") minPrice: Long?,
        @Param("maxPrice") maxPrice: Long?,
        pageable: Pageable?
    ): List<Product?>?


    fun findByCategory_CategoryId(categoryId: Long?): List<Product?>?

    fun findAllByOrderByLikeCountDesc(): List<Product?>?
}