package com.example.ssauc.user.history.dto

import lombok.*
import java.time.LocalDateTime

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
class UserReportDto {
    public val reportId: Long? = null
    public val reportedUserName: String? = null
    public val profileImageUrl: String? = null
    public val reportReason: String? = null
    public val reportDate: LocalDateTime? = null
    public val processedAt: LocalDateTime? = null
    public val status: String? = null
}
