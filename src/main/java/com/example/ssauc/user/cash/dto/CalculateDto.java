package com.example.ssauc.user.cash.dto;

import java.time.LocalDateTime;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CalculateDto {
    private Long orderId;
    private Long paymentAmount;
    private Long productId;
    private String productName;     // orders에서 가져온 상품 이름
    private String productImageUrl;
    private LocalDateTime paymentTime;  // 판매자: orders.completedDate, 구매자: payment.paymentDate
    private String orderStatus;     // orders.order_status
}
