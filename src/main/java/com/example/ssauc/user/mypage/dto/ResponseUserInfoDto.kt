package com.example.ssauc.user.mypage.dto

import lombok.Builder
import lombok.Data

@Data
@Builder
class ResponseUserInfoDto {
    private val userName: String? = null
    private val profileImage: String? = null
    private val reputation: Double? = null
    private val location: String? = null
    private val createdAt: String? = null
    private val lastLogin: String? = null
    private val reviewSummary: String? = null
}
