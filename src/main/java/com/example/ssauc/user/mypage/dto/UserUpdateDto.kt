package com.example.ssauc.user.mypage.dto

import lombok.Data

@Data
class UserUpdateDto {
    private val userName: String? = null // 닉네임
    private val password: String? = null // 새 비밀번호
    private val confirmPassword: String? = null
    private val phone: String? = null // 새 휴대폰 번호
    private val zipcode: String? = null
    private val address: String? = null
    private val addressDetail: String? = null
    private val profileImage: String? = null // 업로드된 이미지 URL
    private val firebaseToken: String? = null // 휴대폰 인증 토큰(선택)
}
