package com.example.ssauc.user.main.repository;

import com.example.ssauc.user.login.entity.Users;
import com.example.ssauc.user.main.entity.ProductLike;
import com.example.ssauc.user.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductLikeRepository extends JpaRepository<ProductLike, Long> {
    Optional<ProductLike> findByUserAndProduct(Users user, Product product);


    @Query("SELECT COUNT(pl) FROM ProductLike pl WHERE pl.product.productId = :productId AND pl.user.userId = :userId")
    int countByProductIdAndUserId(@Param("productId") Long productId, @Param("userId") Long userId);

    List<ProductLike> findByUser_UserId(Long userId);
}