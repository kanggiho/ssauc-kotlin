package com.example.ssauc.user.history.dto;

import java.time.LocalDateTime;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BuyBidHistoryDto {
    private Long bidId;           // 입찰 ID
    private Long productId;       // 상품 ID
    private String productName;   // 상품 이름
    private String productImageUrl;
    private String sellerName;    // 판매자 이름 (Product.seller)
    private String profileImageUrl;
    private LocalDateTime endAt;  // 상품의 경매 마감 시간
    private Long tempPrice;       // 현재가 (temp_price)
    private Long bidPrice;        // 내 입찰가
    private Long maxBidAmount;    // 자동입찰 한도

}
