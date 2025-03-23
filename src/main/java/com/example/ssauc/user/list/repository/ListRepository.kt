package com.example.ssauc.user.list.repository;

import com.example.ssauc.user.list.dto.ListDto;
import com.example.ssauc.user.list.dto.WithLikeDto;
import com.example.ssauc.user.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.JpaQueryLookupStrategy;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ListRepository extends JpaRepository<Product, Long> {

    @Query("SELECT new com.example.ssauc.user.list.dto.WithLikeDto(" +
            "p.productId, p.imageUrl, p.name, p.price, p.bidCount," +
            "p.endAt, p.createdAt, u.location, p.likeCount, p.status," +
            "CASE WHEN pl.user.userId IS NOT NULL THEN true ELSE false END" +
            ") " +
            "FROM Product p " +
            "LEFT JOIN p.likedProducts pl ON pl.user.userId = :userId " +
            "JOIN p.seller u")
    Page<WithLikeDto> getWithLikeProductList(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT new com.example.ssauc.user.list.dto.WithLikeDto(" +
            "p.productId, p.imageUrl, p.name, p.price, p.bidCount, " +
            "p.endAt, p.createdAt, u.location, p.likeCount, p.status, " +
            "false) " +  // 로그인 안 했으므로 'liked'는 항상 false
            "FROM Product p " +
            "JOIN p.seller u")
    Page<WithLikeDto> getAllProductsWithoutUser(Pageable pageable);

    @Query("SELECT new com.example.ssauc.user.list.dto.WithLikeDto(" +
            "p.productId, p.imageUrl, p.name, p.price, p.bidCount," +
            "p.endAt, p.createdAt, u.location, p.likeCount, p.status," +
            "CASE WHEN pl.user.userId IS NOT NULL THEN true ELSE false END" +
            ") " +
            "FROM Product p " +
            "JOIN p.likedProducts pl ON pl.user.userId = :userId " +
            "JOIN p.seller u")
    Page<WithLikeDto> getLikeList(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT new com.example.ssauc.user.list.dto.WithLikeDto(" +
            "p.productId, p.imageUrl, p.name, p.price, p.bidCount," +
            "p.endAt, p.createdAt, u.location, p.likeCount, p.status," +
            "CASE WHEN pl.user.userId IS NOT NULL THEN true ELSE false END" +
            ") " +
            "FROM Product p " +
            "LEFT JOIN p.likedProducts pl ON pl.user.userId = :userId " +
            "JOIN p.seller u " +
            "WHERE p.category.categoryId = :categoryId")
    Page<WithLikeDto> getCategoryList(@Param("userId") Long userId, @Param("categoryId") Long categoryId, Pageable pageable);

    @Query("SELECT new com.example.ssauc.user.list.dto.WithLikeDto(" +
            "p.productId, p.imageUrl, p.name, p.price, p.bidCount," +
            "p.endAt, p.createdAt, u.location, p.likeCount, p.status," +
            "false" +
            ") " +
            "FROM Product p " +
            "JOIN p.seller u " +
            "WHERE p.category.categoryId = :categoryId")
    Page<WithLikeDto> getCategoryListWithoutUser(@Param("categoryId") Long categoryId, Pageable pageable);

    @Query("SELECT new com.example.ssauc.user.list.dto.WithLikeDto(" +
            "p.productId, p.imageUrl, p.name, p.price, p.bidCount," +
            "p.endAt, p.createdAt, u.location, p.likeCount, p.status," +
            "CASE WHEN pl.user.userId IS NOT NULL THEN true ELSE false END" +
            ") " +
            "FROM Product p " +
            "LEFT JOIN p.likedProducts pl ON pl.user.userId = :userId " +
            "JOIN p.seller u " +
            "WHERE p.price BETWEEN :minPrice AND :maxPrice")
    Page<WithLikeDto> findByPriceRange(@Param("userId") Long userId, @Param("minPrice") int minPrice, @Param("maxPrice") int maxPrice, Pageable pageable);

    @Query("SELECT new com.example.ssauc.user.list.dto.WithLikeDto(" +
            "p.productId, p.imageUrl, p.name, p.price, p.bidCount," +
            "p.endAt, p.createdAt, u.location, p.likeCount, p.status," +
            "false" +
            ") " +
            "FROM Product p " +
            "JOIN p.seller u " +
            "WHERE p.price BETWEEN :minPrice AND :maxPrice")
    Page<WithLikeDto> findByPriceRangeWithUserId(@Param("minPrice") int minPrice, @Param("maxPrice") int maxPrice, Pageable pageable);

    @Query("SELECT new com.example.ssauc.user.list.dto.ListDto("
            + "p.productId, p.imageUrl, p.name, p.price, p.bidCount,"
            + "p.endAt, p.createdAt, u.location, p.likeCount, p.status) "
            + "FROM Product p "
            + "JOIN p.seller u "
            + "WHERE p.endAt > CURRENT_TIMESTAMP and p.status = '판매중'") // ✅ 마감되지 않은 상품만 필터링
    Page<ListDto> getAvailableProductList(Pageable pageable);

    @Query("SELECT new com.example.ssauc.user.list.dto.WithLikeDto(" +
            "p.productId, p.imageUrl, p.name, p.price, p.bidCount," +
            "p.endAt, p.createdAt, u.location, p.likeCount, p.status," +
            "CASE WHEN pl.user.userId IS NOT NULL THEN true ELSE false END" +
            ") " +
            "FROM Product p " +
            "LEFT JOIN p.likedProducts pl ON pl.user.userId = :userId " +
            "JOIN p.seller u " +
            "WHERE p.endAt > CURRENT_TIMESTAMP and p.status = '판매중'")
    Page<WithLikeDto> getAvailableProductListWithLike(@Param("userId") Long userId, Pageable pageable);
}