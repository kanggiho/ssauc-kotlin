package com.example.ssauc.user.search.dto;

import com.example.ssauc.user.product.entity.Category;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@ToString
public class ProductDTO {
    private Long productId;       // 상품 ID
    private String name;          // 상품명
    private String description;   // 상품 설명
    private String categoryName;  // 카테고리명
    private Long price;           // 즉시 구매가
    private Long startPrice;      // 경매 시작가
    private Long tempPrice;       // 현재 입찰가 (입찰 중인 경우)
    private int minIncrement;     // 최소 입찰 증가 단위
    private int bidCount;         // 입찰 수
    private int likeCount;        // 좋아요 개수
    private String imageUrl;      // 상품 이미지 URL
    private Long viewCount;       // 조회수
    private String status;        // 판매 상태 (판매 중, 판매 완료 등)
    private String location;      // 지역
}
