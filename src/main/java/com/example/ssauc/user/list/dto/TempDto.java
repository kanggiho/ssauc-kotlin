package com.example.ssauc.user.list.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TempDto {
    private Long productId; // 다 Product 테이블
    private String imageUrl;
    private String name;
    private String price;
    private String bidCount;
    private String gap;
    private String location; // Users 테이블
    private String likeCount;
    private boolean liked;
    private String status;
}
