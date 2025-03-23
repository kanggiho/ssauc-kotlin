package com.example.ssauc.user.history.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class SoldDetailDto {
    // Product 테이블 관련 필드
    private Long productId;
    private String productName;
    private Long startPrice;
    private LocalDateTime createdAt;
    private int dealType;
    private String imageUrl;

    // Orders 테이블 관련 필드
    private Long orderId;
    private String buyerName;
    private Long totalPrice;
    private String recipientName;
    private String recipientPhone;
    private String postalCode;
    private String deliveryAddress;
    private String deliveryStatus;
    private LocalDateTime orderDate;
    private LocalDateTime completedDate;
}

