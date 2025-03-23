package com.example.ssauc.user.cash.dto

import lombok.Data

@Data
class ChargeRequestDto {
    // 결제 완료 시 클라이언트에서 서버로 전달되는 정보
    private val paymentId: String? = null
    private val amount: Long? = null
}