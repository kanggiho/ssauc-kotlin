package com.example.ssauc.user.login.dto

import lombok.Data

@Data
class UserRegistrationDTO {
    private val userName: String? = null
    private val email: String? = null
    private val password: String? = null
    private val confirmPassword: String? = null
    private val phone: String? = null
    private val smsCode: String? = null

    // Firebase에서 받은 Phone 인증 토큰 (IdToken)
    private val firebaseToken: String? = null

    // 주소 입력 관련 필드 (우편번호, 기본주소, 상세주소)
    private val zipcode: String? = null // 우편번호
    private val address: String? = null // 기본주소 (Daum Postcode API로 선택)
    private val addressDetail: String? = null // 상세주소 (사용자 입력)
}
