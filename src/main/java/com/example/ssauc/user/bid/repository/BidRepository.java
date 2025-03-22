package com.example.ssauc.user.bid.repository;

import com.example.ssauc.user.bid.entity.Bid;
import com.example.ssauc.user.product.entity.Product;
import com.example.ssauc.user.login.entity.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BidRepository extends JpaRepository<Bid, Long> {

    @Query("SELECT b.user.userId FROM Bid b WHERE b.bidPrice = (SELECT MAX(b2.bidPrice) FROM Bid b2 WHERE b2.product.productId = :productId) AND b.product.productId = :productId")
    Long findUserIdWithHighestBidPrice(@Param("productId") Long productId);


    Optional<Bid> findTopByProductOrderByBidTimeDesc(Product product);

    // 같은 상품(product)에 대한 경매 마감 시간이 지나지 않고
    // 입찰 시간(bidTime)이 가장 큰(최신) 입찰만 선택
    @Query("select b from Bid b " +
            "where b.user = :buyer " +
            "  and b.product.endAt > :now " +
            "  and b.bidTime = (select max(b2.bidTime) from Bid b2 " +
            "                   where b2.user = :buyer and b2.product = b.product)")
    Page<Bid> findLatestBidsByUser(@Param("buyer") Users buyer,
                                   @Param("now") LocalDateTime now,
                                   Pageable pageable);


    @Query("SELECT DISTINCT b.user.userId " +
            "FROM Bid b " +
            "WHERE b.product.productId = :productId " +
            "AND b.user.userId <> :userId")
    List<Long> findDistinctUserIdByProductIdAndNotUserId(@Param("productId") Long productId,
                                                         @Param("userId") Long userId);


    @Query("SELECT DISTINCT b.user FROM Bid b WHERE b.product.productId = :productId")
    List<Users> findUserIdsByProductId(@Param("productId") Long productId);


}