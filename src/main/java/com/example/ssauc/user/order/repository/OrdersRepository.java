package com.example.ssauc.user.order.repository;

import com.example.ssauc.user.login.entity.Users;
import com.example.ssauc.user.order.entity.Orders;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface OrdersRepository extends JpaRepository<Orders, Long> {

    // 결제 내역 (구매자 기준 주문) 조회
    Page<Orders> findByBuyer(Users buyer, Pageable pageable);

    @Query("SELECT o FROM Orders o LEFT JOIN o.payments p " +
            "WHERE o.buyer = :buyer AND p.paymentDate BETWEEN :start AND :end")
    Page<Orders> findByBuyerAndPaymentTimeBetween(@Param("buyer") Users buyer,
                                                  @Param("start") LocalDateTime start,
                                                  @Param("end") LocalDateTime end,
                                                  Pageable pageable);
    // 정산 내역 (판매자 기준 주문) 조회
    Page<Orders> findBySeller(Users seller, Pageable pageable);

    @Query("SELECT o FROM Orders o LEFT JOIN o.payments p " +
            "WHERE o.seller = :seller AND p.paymentDate BETWEEN :start AND :end")
    Page<Orders> findBySellerAndPaymentTimeBetween(@Param("seller") Users seller,
                                                   @Param("start") LocalDateTime start,
                                                   @Param("end") LocalDateTime end,
                                                   Pageable pageable);
    // 기간 별 총 금액 계산
    // 결제 내역 총 금액
    @Query("SELECT COALESCE(SUM(o.totalPrice), 0) FROM Orders o " +
            "WHERE o.buyer = :buyer  AND o.orderStatus = '성공'")
    long sumTotalPriceByBuyer(Users buyer);

    @Query("SELECT COALESCE(SUM(o.totalPrice), 0) FROM Orders o LEFT JOIN o.payments p " +
            "WHERE o.buyer = :buyer AND p.paymentDate BETWEEN :start AND :end AND o.orderStatus = '성공'")
    long sumTotalPriceByBuyerAndPaymentDateBetween(@Param("buyer") Users buyer,
                                                   @Param("start") LocalDateTime start,
                                                   @Param("end") LocalDateTime end);
    // 정산 내역 총 금액
    @Query("SELECT COALESCE(SUM(o.totalPrice), 0) FROM Orders o " +
            "WHERE o.seller = :seller AND o.orderStatus = '성공'")
    long sumTotalPriceBySeller(Users seller);

    @Query("SELECT COALESCE(SUM(o.totalPrice), 0) FROM Orders o LEFT JOIN o.payments p " +
            "WHERE o.seller = :seller AND p.paymentDate BETWEEN :start AND :end AND o.orderStatus = '성공'")
    long sumTotalPriceBySellerAndPaymentDateBetween(@Param("seller") Users seller,
                                                    @Param("start") LocalDateTime start,
                                                    @Param("end") LocalDateTime end);
    // 주문 상태가 "거래완료"인 주문 페이징 처리
    Page<Orders> findBySellerAndOrderStatus(Users seller, String orderStatus, Pageable pageable);


    // 로그인 사용자가 buyer 또는 seller인 주문 중 아직 리뷰를 작성하지 않은 주문(pending 상태) 조회
    @Query("SELECT o FROM Orders o LEFT JOIN o.reviews r WITH r.reviewer.userId = :userId " +
            "WHERE (o.buyer.userId = :userId OR o.seller.userId = :userId) AND r IS NULL")
    Page<Orders> findPendingReviewOrders(@Param("userId") Long userId, Pageable pageable);
}