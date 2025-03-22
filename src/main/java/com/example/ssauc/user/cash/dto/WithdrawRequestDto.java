package com.example.ssauc.user.cash.dto;

import lombok.Data;

@Data
public class WithdrawRequestDto {
    // 환급 신청 시 클라이언트에서 서버로 전달되는 정보
    private Long amount;
    private String bank;
    private String account;
}