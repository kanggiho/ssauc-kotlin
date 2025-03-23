package com.example.ssauc.user.history.dto;

import java.time.LocalDateTime;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BuyHistoryDto {
    private Long orderId;
    private Long productId;
    private String productName;
    private String productImageUrl;
    private String sellerName;
    private String profileImageUrl;
    private Long totalPrice;
    private LocalDateTime orderDate;
    private LocalDateTime completedDate;
}
