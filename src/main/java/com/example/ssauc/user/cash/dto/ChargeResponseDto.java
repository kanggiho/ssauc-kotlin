package com.example.ssauc.user.cash.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChargeResponseDto {
    // 결제 완료 후 서버에서 클라이언트로 반환하는 응답 데이터
    private String status;
    private Long chargeId;
}
