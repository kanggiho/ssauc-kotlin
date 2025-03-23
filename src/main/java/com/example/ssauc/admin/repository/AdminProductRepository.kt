package com.example.ssauc.admin.repository;

import com.example.ssauc.user.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface AdminProductRepository extends JpaRepository<Product, Long> {
  @EntityGraph(attributePaths = {"seller"})
  Page<Product> findAll(Pageable pageable);

  @Transactional
  @Modifying
  @Query("UPDATE Product p SET p.status = :status WHERE p.productId = :productId")
  int updateProductByProductId(@Param("status") String status, @Param("productId") Long productId);

  Page<Product> findByNameContainingIgnoreCase(String keyword, Pageable pageable);
}