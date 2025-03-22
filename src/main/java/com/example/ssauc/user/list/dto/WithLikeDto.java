package com.example.ssauc.user.list.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WithLikeDto {
    private Long productId;
    private String imageUrl;
    private String name;
    private Long price;
    private int bidCount;
    private LocalDateTime endAt;
    private LocalDateTime createdAt;
    private String location;
    private int likeCount;
    private String status;
    private boolean liked;
}
