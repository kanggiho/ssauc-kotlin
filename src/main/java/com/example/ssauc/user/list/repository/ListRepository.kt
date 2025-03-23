package com.example.ssauc.user.list.repository

import com.example.ssauc.user.list.dto.ListDto
import com.example.ssauc.user.list.dto.WithLikeDto
import com.example.ssauc.user.product.entity.Product
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface ListRepository : JpaRepository<Product?, Long?> {
    @Query(
        ("SELECT new com.example.ssauc.user.list.dto.WithLikeDto(" +
                "p.productId, p.imageUrl, p.name, p.price, p.bidCount," +
                "p.endAt, p.createdAt, u.location, p.likeCount, p.status," +
                "CASE WHEN pl.user.userId IS NOT NULL THEN true ELSE false END" +
                ") " +
                "FROM Product p " +
                "LEFT JOIN p.likedProducts pl ON pl.user.userId = :userId " +
                "JOIN p.seller u")
    )
    fun getWithLikeProductList(@Param("userId") userId: Long?, pageable: Pageable?): Page<WithLikeDto?>?

    @Query(
        ("SELECT new com.example.ssauc.user.list.dto.WithLikeDto(" +
                "p.productId, p.imageUrl, p.name, p.price, p.bidCount, " +
                "p.endAt, p.createdAt, u.location, p.likeCount, p.status, " +
                "false) " +  // 로그인 안 했으므로 'liked'는 항상 false
                "FROM Product p " +
                "JOIN p.seller u")
    )
    fun getAllProductsWithoutUser(pageable: Pageable?): Page<WithLikeDto?>?

    @Query(
        ("SELECT new com.example.ssauc.user.list.dto.WithLikeDto(" +
                "p.productId, p.imageUrl, p.name, p.price, p.bidCount," +
                "p.endAt, p.createdAt, u.location, p.likeCount, p.status," +
                "CASE WHEN pl.user.userId IS NOT NULL THEN true ELSE false END" +
                ") " +
                "FROM Product p " +
                "JOIN p.likedProducts pl ON pl.user.userId = :userId " +
                "JOIN p.seller u")
    )
    fun getLikeList(@Param("userId") userId: Long?, pageable: Pageable?): Page<WithLikeDto?>?

    @Query(
        ("SELECT new com.example.ssauc.user.list.dto.WithLikeDto(" +
                "p.productId, p.imageUrl, p.name, p.price, p.bidCount," +
                "p.endAt, p.createdAt, u.location, p.likeCount, p.status," +
                "CASE WHEN pl.user.userId IS NOT NULL THEN true ELSE false END" +
                ") " +
                "FROM Product p " +
                "LEFT JOIN p.likedProducts pl ON pl.user.userId = :userId " +
                "JOIN p.seller u " +
                "WHERE p.category.categoryId = :categoryId")
    )
    fun getCategoryList(
        @Param("userId") userId: Long?,
        @Param("categoryId") categoryId: Long?,
        pageable: Pageable?
    ): Page<WithLikeDto?>?

    @Query(
        ("SELECT new com.example.ssauc.user.list.dto.WithLikeDto(" +
                "p.productId, p.imageUrl, p.name, p.price, p.bidCount," +
                "p.endAt, p.createdAt, u.location, p.likeCount, p.status," +
                "false" +
                ") " +
                "FROM Product p " +
                "JOIN p.seller u " +
                "WHERE p.category.categoryId = :categoryId")
    )
    fun getCategoryListWithoutUser(@Param("categoryId") categoryId: Long?, pageable: Pageable?): Page<WithLikeDto?>?

    @Query(
        ("SELECT new com.example.ssauc.user.list.dto.WithLikeDto(" +
                "p.productId, p.imageUrl, p.name, p.price, p.bidCount," +
                "p.endAt, p.createdAt, u.location, p.likeCount, p.status," +
                "CASE WHEN pl.user.userId IS NOT NULL THEN true ELSE false END" +
                ") " +
                "FROM Product p " +
                "LEFT JOIN p.likedProducts pl ON pl.user.userId = :userId " +
                "JOIN p.seller u " +
                "WHERE p.price BETWEEN :minPrice AND :maxPrice")
    )
    fun findByPriceRange(
        @Param("userId") userId: Long?,
        @Param("minPrice") minPrice: Int,
        @Param("maxPrice") maxPrice: Int,
        pageable: Pageable?
    ): Page<WithLikeDto?>?

    @Query(
        ("SELECT new com.example.ssauc.user.list.dto.WithLikeDto(" +
                "p.productId, p.imageUrl, p.name, p.price, p.bidCount," +
                "p.endAt, p.createdAt, u.location, p.likeCount, p.status," +
                "false" +
                ") " +
                "FROM Product p " +
                "JOIN p.seller u " +
                "WHERE p.price BETWEEN :minPrice AND :maxPrice")
    )
    fun findByPriceRangeWithUserId(
        @Param("minPrice") minPrice: Int,
        @Param("maxPrice") maxPrice: Int,
        pageable: Pageable?
    ): Page<WithLikeDto?>?

    @Query(
        ("SELECT new com.example.ssauc.user.list.dto.ListDto("
                + "p.productId, p.imageUrl, p.name, p.price, p.bidCount,"
                + "p.endAt, p.createdAt, u.location, p.likeCount, p.status) "
                + "FROM Product p "
                + "JOIN p.seller u "
                + "WHERE p.endAt > CURRENT_TIMESTAMP and p.status = '판매중'")
    )
    fun getAvailableProductList(pageable: Pageable?): Page<ListDto?>?

    @Query(
        ("SELECT new com.example.ssauc.user.list.dto.WithLikeDto(" +
                "p.productId, p.imageUrl, p.name, p.price, p.bidCount," +
                "p.endAt, p.createdAt, u.location, p.likeCount, p.status," +
                "CASE WHEN pl.user.userId IS NOT NULL THEN true ELSE false END" +
                ") " +
                "FROM Product p " +
                "LEFT JOIN p.likedProducts pl ON pl.user.userId = :userId " +
                "JOIN p.seller u " +
                "WHERE p.endAt > CURRENT_TIMESTAMP and p.status = '판매중'")
    )
    fun getAvailableProductListWithLike(@Param("userId") userId: Long?, pageable: Pageable?): Page<WithLikeDto?>?
}