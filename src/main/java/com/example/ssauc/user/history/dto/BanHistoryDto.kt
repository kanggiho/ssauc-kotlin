package com.example.ssauc.user.history.dto

import lombok.AllArgsConstructor
import lombok.Data
import lombok.NoArgsConstructor
import java.time.LocalDateTime

@Data
@NoArgsConstructor
@AllArgsConstructor
class BanHistoryDto {
    public val banId: Long? = null
    public val blockedUserName: String? = null
    public val profileImage: String? = null
    public val blockedAt: LocalDateTime? = null
}
