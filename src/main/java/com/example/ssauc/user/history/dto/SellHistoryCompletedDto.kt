package com.example.ssauc.user.history.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SellHistoryCompletedDto {
    private Long orderId;
    private Long productId;
    private String productName;
    private String productImageUrl;
    private String buyerName;
    private String profileImageUrl;
    private Long totalPrice;
    private LocalDateTime orderDate;
    private LocalDateTime completedDate;
}
