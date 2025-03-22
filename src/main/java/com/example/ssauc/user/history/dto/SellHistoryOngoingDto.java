package com.example.ssauc.user.history.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SellHistoryOngoingDto {
    private Long productId;
    private String productName;
    private String productImageUrl;
    private Long tempPrice;
    private Long price;
    private LocalDateTime createdAt;
    private LocalDateTime endAt;
}
