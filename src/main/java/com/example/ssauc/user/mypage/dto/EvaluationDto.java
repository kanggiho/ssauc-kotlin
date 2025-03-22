package com.example.ssauc.user.mypage.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvaluationDto { // 리뷰 작성
    private Long orderId;
    private Long productId;
    // 거래 유형은 프론트엔드에서 전달하지만 실제 DB에는 저장하지 않으므로, 주로 판단용으로 사용
    private String transactionType;
    // q1, q2, q3는 "positive" 또는 "negative" 문자열이 전달됨
    private String q1;
    private String q2;
    private String q3;
    // 사용자가 입력한 상세 후기 (최대 300자)
    private String reviewContent;
    // 프론트엔드에서 baseScore를 계산하여 전달 (예: 0.5 + 각 옵션에 따라 ±0.5)
    private Double baseScore;

    // 화면에 표시할 추가 정보
    private String productName;
    private String otherUserName;
}
