package com.example.ssauc.user.main.repository

import com.example.ssauc.user.login.entity.Users
import com.example.ssauc.user.main.entity.ProductLike
import com.example.ssauc.user.product.entity.Product
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface ProductLikeRepository : JpaRepository<ProductLike?, Long?> {
    fun findByUserAndProduct(user: Users?, product: Product?): Optional<ProductLike?>?


    @Query("SELECT COUNT(pl) FROM ProductLike pl WHERE pl.product.productId = :productId AND pl.user.userId = :userId")
    fun countByProductIdAndUserId(@Param("productId") productId: Long?, @Param("userId") userId: Long?): Int

    fun findByUser_UserId(userId: Long?): List<ProductLike?>?
}