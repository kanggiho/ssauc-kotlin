package com.example.ssauc.user.list.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ListDto {
    private Long productId; // 다 Product 테이블
    private String imageUrl;
    private String name;
    private Long price;
    private int bidCount;
    private LocalDateTime endAt;
    private LocalDateTime createdAt;
    private String location; // Users 테이블
    private int likeCount;
    private String status;
}
