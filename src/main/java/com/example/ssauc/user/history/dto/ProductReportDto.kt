package com.example.ssauc.user.history.dto

import lombok.*
import java.time.LocalDateTime

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
class ProductReportDto {
    public val reportId: Long? = null
    public val productId: Long? = null
    public val productName: String? = null
    public val productImageUrl: String? = null
    public val reportReason: String? = null
    public val reportDate: LocalDateTime? = null
    public val processedAt: LocalDateTime? = null
    public val status: String? = null
}
