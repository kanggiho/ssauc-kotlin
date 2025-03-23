package com.example.ssauc.user.bid.repository

import com.example.ssauc.user.bid.entity.Bid
import com.example.ssauc.user.login.entity.Users
import com.example.ssauc.user.product.entity.Product
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime
import java.util.*

interface BidRepository : JpaRepository<Bid?, Long?> {
    @Query("SELECT b.user.userId FROM Bid b WHERE b.bidPrice = (SELECT MAX(b2.bidPrice) FROM Bid b2 WHERE b2.product.productId = :productId) AND b.product.productId = :productId")
    fun findUserIdWithHighestBidPrice(@Param("productId") productId: Long?): Long?


    fun findTopByProductOrderByBidTimeDesc(product: Product?): Optional<Bid?>?

    // 같은 상품(product)에 대한 경매 마감 시간이 지나지 않고
    // 입찰 시간(bidTime)이 가장 큰(최신) 입찰만 선택
    @Query(
        ("select b from Bid b " +
                "where b.user = :buyer " +
                "  and b.product.endAt > :now " +
                "  and b.bidTime = (select max(b2.bidTime) from Bid b2 " +
                "                   where b2.user = :buyer and b2.product = b.product)")
    )
    fun findLatestBidsByUser(
        @Param("buyer") buyer: Users?,
        @Param("now") now: LocalDateTime?,
        pageable: Pageable?
    ): Page<Bid?>?


    @Query(
        ("SELECT DISTINCT b.user.userId " +
                "FROM Bid b " +
                "WHERE b.product.productId = :productId " +
                "AND b.user.userId <> :userId")
    )
    fun findDistinctUserIdByProductIdAndNotUserId(
        @Param("productId") productId: Long?,
        @Param("userId") userId: Long?
    ): List<Long?>?


    @Query("SELECT DISTINCT b.user FROM Bid b WHERE b.product.productId = :productId")
    fun findUserIdsByProductId(@Param("productId") productId: Long?): List<Users?>?
}