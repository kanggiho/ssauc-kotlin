package com.example.ssauc.user.cash.dto

import lombok.*
import java.time.LocalDateTime

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
class ChargeDto {
    private val chargeId: Long? = null
    private val chargeType: String? = null
    private val amount: Long? = null
    private val status: String? = null
    private val createdAt: LocalDateTime? = null
    private val receiptUrl: String? = null
}
