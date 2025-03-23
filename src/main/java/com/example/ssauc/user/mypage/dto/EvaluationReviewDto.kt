package com.example.ssauc.user.mypage.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvaluationReviewDto { // 작성된 리뷰 리스트
    // 리뷰 상세 페이지 이동용 식별자
    private Long reviewId;
    // 해당 리뷰가 연결된 주문번호
    private Long orderId;
    // 리뷰 대상:
    // - written: 리뷰 작성 시 대상은 review.getReviewee()의 이름
    // - received: 리뷰 수신 시 대상은 review.getReviewer()의 이름
    private String reviewer;
    private String reviewee;
    private String profileImageUrl1;
    private String profileImageUrl2;
    // 주문한 상품의 이름 (orders → product)
    private Long productId;
    private String productName;
    private String productImageUrl;
    // 리뷰 작성일
    private LocalDateTime createdAt;
    private String transactionType;
}
