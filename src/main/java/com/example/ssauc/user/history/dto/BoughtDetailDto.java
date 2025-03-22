package com.example.ssauc.user.history.dto;

import java.time.LocalDateTime;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoughtDetailDto {
    // Product 테이블 정보
    private Long productId;
    private String productName;
    private Long startPrice;
    private LocalDateTime createdAt;
    private int dealType;
    private String imageUrl;

    // 판매자 정보 (Orders.seller 기준)
    private String sellerName;

    // Orders 테이블 정보
    private Long orderId;
    private Long totalPrice;
    private String recipientName;
    private String recipientPhone;
    private String postalCode;
    private String deliveryAddress;
    private LocalDateTime orderDate;
    private LocalDateTime completedDate;
    private String deliveryStatus;
}
