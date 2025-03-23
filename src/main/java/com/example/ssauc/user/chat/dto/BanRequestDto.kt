package com.example.ssauc.user.chat.dto

import lombok.AllArgsConstructor
import lombok.Getter
import lombok.NoArgsConstructor
import lombok.Setter

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
class BanRequestDto {
    private val userId: Long? = null // 차단하는 사용자 ID
    private val blockedUserId: Long? = null // 차단 당하는 사용자 ID
}
