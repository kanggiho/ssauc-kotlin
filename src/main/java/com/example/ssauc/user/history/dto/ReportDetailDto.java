package com.example.ssauc.user.history.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportDetailDto {
    // "product" 또는 "user" 값으로 신고 유형 구분
    private String type;
    // 상품 신고인 경우
    private Long productId;
    private String productName;
    // 유저 신고인 경우
    private String reportedUserName;
    private String reportReason;
    private String details;
    private LocalDateTime reportDate;
    private String status;
    private LocalDateTime processedAt;
}
