package com.example.ssauc.user.bid.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductInformDto {

    // ====================== 상품 정보 탭 ======================
    private String name; // 상품 이름

    private Long tempPrice; // 현재 입찰가

    private LocalDateTime createdAt; // 등록 시간

    private LocalDateTime endAt; // 마감 시간

    private Long totalTime; // 경매 남은 시간 (초단위)

    private Long price; // 즉시 구매가

    private String imageUrl; // 사진 정보

    private int bidCount; // 입찰 수

    private int dealType; // 거래 방식

    private int minIncrement; // 최소 입찰 단위

    // ====================== 판매자 정보 탭 ======================
    private String userName; // 회원 이름

    private String profileImage; // 프로필 사진

    private Double reputation; // 평가 지표

    // ====================== 정보 설명 탭 ======================
    private String description; // 상품 설명

    private String location; // 지역

    private Long viewCount; // 조회수

    private int likeCount; // 좋아요 수
}
