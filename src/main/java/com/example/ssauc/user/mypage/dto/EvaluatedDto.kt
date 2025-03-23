package com.example.ssauc.user.mypage.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvaluatedDto {
    private Long reviewId;
    private Long orderId;
    private String reviewerName;
    private String revieweeName;
    private String profileImageUrl;
    private Long productId;
    private String productName;
    private String productImageUrl;
    private LocalDateTime createdAt;
    private String transactionType;
    private Boolean option1;
    private Boolean option2;
    private Boolean option3;
    private String comment;
}
