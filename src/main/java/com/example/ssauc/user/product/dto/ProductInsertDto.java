package com.example.ssauc.user.product.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductInsertDto {
    // 클라이언트에서 전달받은 카테고리 이름
    private String categoryName;
    // 상품 제목
    private String name;
    // 상품 설명
    private String description;
    // 즉시 구매가
    private Long price;
    // 현재 입찰가
    private Long tempPrice;
    // 경매 시작가
    private Long startPrice;
    // 첨부 이미지 파일명(향후 S3 연동 시 URL로 대체)
    private String imageUrl;

    // 마감 시간 관련 데이터
    private String auctionDate;  // YYYY-MM-DD 형식
    private Integer auctionHour;
    private Integer auctionMinute;

    // 최소 입찰단위
    private int minIncrement;
    // 거래 유형 (0: 직거래, 1: 택배, 2: 둘 다 선택)
    private int dealType;
}
