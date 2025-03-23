package com.example.ssauc.user.bid.dto

import lombok.AllArgsConstructor
import lombok.Getter
import lombok.NoArgsConstructor
import lombok.Setter


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class ReportDto {
    public val productId: Long? = null
    public val reporterId: Long? = null
    public val reportedUserId: Long? = null
    public val reportReason: String? = null
    public val details: String? = null
}
