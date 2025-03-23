package com.example.ssauc.user.bid.repository;

import com.example.ssauc.user.bid.dto.ProductInformDto;
import com.example.ssauc.user.list.dto.ListDto;
import com.example.ssauc.user.product.entity.Product;
import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PdpRepository extends JpaRepository<Product, Long> {

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT p FROM Product p WHERE p.productId = :productId")
  Optional<Product> findProductForUpdate(@Param("productId") Long productId);


  @Query("SELECT new com.example.ssauc.user.bid.dto.ProductInformDto("
          + "p.name, p.tempPrice, p.createdAt, p.endAt, p.startPrice, p.price, p.imageUrl, "
          + "p.bidCount, p.dealType, p.minIncrement, u.userName, u.profileImage, "
          + "u.reputation, p.description, u.location, p.viewCount, p.likeCount)"
          + "FROM Product p "
          + "JOIN p.seller u "
          + "WHERE p.productId = :productId")
  ProductInformDto getPdpInform(@Param("productId") Long productId);

  @Modifying
  @Transactional
  @Query("UPDATE Product p SET p.tempPrice = :tempPrice, p.bidCount = p.bidCount + 1 WHERE p.productId = :productId")
  int updateProductField(@Param("tempPrice") Long tempPrice, @Param("productId") Long productId);


  @Modifying
  @Transactional
  @Query("UPDATE Product p SET p.endAt = :newEndAt WHERE p.productId = :productId")
  void updateEndAt(@Param("productId") Long productId, @Param("newEndAt") LocalDateTime newEndAt);



}