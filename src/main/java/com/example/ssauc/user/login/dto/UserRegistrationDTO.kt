package com.example.ssauc.user.login.dto;

import lombok.Data;

@Data
public class UserRegistrationDTO {
    private String userName;
    private String email;
    private String password;
    private String confirmPassword;
    private String phone;
    private String smsCode;
    // Firebase에서 받은 Phone 인증 토큰 (IdToken)
    private String firebaseToken;

    // 주소 입력 관련 필드 (우편번호, 기본주소, 상세주소)
    private String zipcode;       // 우편번호
    private String address;       // 기본주소 (Daum Postcode API로 선택)
    private String addressDetail; // 상세주소 (사용자 입력)
}
