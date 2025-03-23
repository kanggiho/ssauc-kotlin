package com.example.ssauc.user.history.dto

import lombok.*
import java.time.LocalDateTime

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
class ReportDetailDto {
    // "product" 또는 "user" 값으로 신고 유형 구분
    public val type: String? = null

    // 상품 신고인 경우
    public val productId: Long? = null
    public val productName: String? = null

    // 유저 신고인 경우
    public val reportedUserName: String? = null
    public val reportReason: String? = null
    public val details: String? = null
    public val reportDate: LocalDateTime? = null
    public val status: String? = null
    public val processedAt: LocalDateTime? = null
}
