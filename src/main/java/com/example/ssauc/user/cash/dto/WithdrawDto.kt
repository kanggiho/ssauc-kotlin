package com.example.ssauc.user.cash.dto

import lombok.*
import java.time.LocalDateTime

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
class WithdrawDto {
    private val withdrawId: Long? = null
    private val bank: String? = null
    private val account: String? = null
    private val netAmount: Long? = null // amount - commission
    private val withdrawAt: LocalDateTime? = null
    private val requestStatus: String? = null // withdrawAt != null ? "완료" : "처리중"
}
