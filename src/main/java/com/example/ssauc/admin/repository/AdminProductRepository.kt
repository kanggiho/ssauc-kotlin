package com.example.ssauc.admin.repository

import com.example.ssauc.user.product.entity.Product
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.transaction.annotation.Transactional

interface AdminProductRepository : JpaRepository<Product?, Long?> {
    @EntityGraph(attributePaths = ["seller"])
    override fun findAll(pageable: Pageable): Page<Product?>

    @Transactional
    @Modifying
    @Query("UPDATE Product p SET p.status = :status WHERE p.productId = :productId")
    fun updateProductByProductId(@Param("status") status: String?, @Param("productId") productId: Long?): Int

    fun findByNameContainingIgnoreCase(keyword: String?, pageable: Pageable?): Page<Product?>?
}