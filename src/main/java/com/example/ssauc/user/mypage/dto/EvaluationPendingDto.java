package com.example.ssauc.user.mypage.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvaluationPendingDto { // 작성할 리뷰 리스트
    // 해당 주문번호
    private Long orderId;
    // 리뷰 대상: 주문에서 로그인한 사용자가 buyer이면 seller의 이름, seller이면 buyer의 이름
    private String reviewTarget;
    private String profileImageUrl;
    // 주문한 상품 이름 (orders → product)
    private Long productId;
    private String productName;
    private String productImageUrl;
    // 주문 일자 (정렬용으로 활용 – 필요에 따라 추가 정보 표시 가능)
    private LocalDateTime orderDate;
    private String transactionType;
}
