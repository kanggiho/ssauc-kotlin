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
public class ProductReportDto {
    private Long reportId;
    private Long productId;
    private String productName;
    private String productImageUrl;
    private String reportReason;
    private LocalDateTime reportDate;
    private LocalDateTime processedAt;
    private String status;
}
