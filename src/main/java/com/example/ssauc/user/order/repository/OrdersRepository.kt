package com.example.ssauc.user.order.repository

import com.example.ssauc.user.login.entity.Users
import com.example.ssauc.user.order.entity.Orders
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime

interface OrdersRepository : JpaRepository<Orders?, Long?> {
    // 결제 내역 (구매자 기준 주문) 조회
    fun findByBuyer(buyer: Users?, pageable: Pageable?): Page<Orders?>?

    @Query(
        "SELECT o FROM Orders o LEFT JOIN o.payments p " +
                "WHERE o.buyer = :buyer AND p.paymentDate BETWEEN :start AND :end"
    )
    fun findByBuyerAndPaymentTimeBetween(
        @Param("buyer") buyer: Users?,
        @Param("start") start: LocalDateTime?,
        @Param("end") end: LocalDateTime?,
        pageable: Pageable?
    ): Page<Orders?>?

    // 정산 내역 (판매자 기준 주문) 조회
    fun findBySeller(seller: Users?, pageable: Pageable?): Page<Orders?>?

    @Query(
        "SELECT o FROM Orders o LEFT JOIN o.payments p " +
                "WHERE o.seller = :seller AND p.paymentDate BETWEEN :start AND :end"
    )
    fun findBySellerAndPaymentTimeBetween(
        @Param("seller") seller: Users?,
        @Param("start") start: LocalDateTime?,
        @Param("end") end: LocalDateTime?,
        pageable: Pageable?
    ): Page<Orders?>?

    // 기간 별 총 금액 계산
    // 결제 내역 총 금액
    @Query(
        "SELECT COALESCE(SUM(o.totalPrice), 0) FROM Orders o " +
                "WHERE o.buyer = :buyer  AND o.orderStatus = '성공'"
    )
    fun sumTotalPriceByBuyer(buyer: Users?): Long

    @Query(
        "SELECT COALESCE(SUM(o.totalPrice), 0) FROM Orders o LEFT JOIN o.payments p " +
                "WHERE o.buyer = :buyer AND p.paymentDate BETWEEN :start AND :end AND o.orderStatus = '성공'"
    )
    fun sumTotalPriceByBuyerAndPaymentDateBetween(
        @Param("buyer") buyer: Users?,
        @Param("start") start: LocalDateTime?,
        @Param("end") end: LocalDateTime?
    ): Long

    // 정산 내역 총 금액
    @Query(
        "SELECT COALESCE(SUM(o.totalPrice), 0) FROM Orders o " +
                "WHERE o.seller = :seller AND o.orderStatus = '성공'"
    )
    fun sumTotalPriceBySeller(seller: Users?): Long

    @Query(
        "SELECT COALESCE(SUM(o.totalPrice), 0) FROM Orders o LEFT JOIN o.payments p " +
                "WHERE o.seller = :seller AND p.paymentDate BETWEEN :start AND :end AND o.orderStatus = '성공'"
    )
    fun sumTotalPriceBySellerAndPaymentDateBetween(
        @Param("seller") seller: Users?,
        @Param("start") start: LocalDateTime?,
        @Param("end") end: LocalDateTime?
    ): Long

    // 주문 상태가 "거래완료"인 주문 페이징 처리
    fun findBySellerAndOrderStatus(seller: Users?, orderStatus: String?, pageable: Pageable?): Page<Orders?>?


    // 로그인 사용자가 buyer 또는 seller인 주문 중 아직 리뷰를 작성하지 않은 주문(pending 상태) 조회
    @Query(
        "SELECT o FROM Orders o LEFT JOIN o.reviews r WITH r.reviewer.userId = :userId " +
                "WHERE (o.buyer.userId = :userId OR o.seller.userId = :userId) AND r IS NULL"
    )
    fun findPendingReviewOrders(@Param("userId") userId: Long?, pageable: Pageable?): Page<Orders?>?
}