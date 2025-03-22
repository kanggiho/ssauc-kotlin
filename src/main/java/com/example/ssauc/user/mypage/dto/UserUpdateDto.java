package com.example.ssauc.user.mypage.dto;

import lombok.Data;

@Data
public class UserUpdateDto {
    private String userName;       // 닉네임
    private String password;       // 새 비밀번호
    private String confirmPassword;
    private String phone;          // 새 휴대폰 번호
    private String zipcode;
    private String address;
    private String addressDetail;
    private String profileImage;   // 업로드된 이미지 URL
    private String firebaseToken;  // 휴대폰 인증 토큰(선택)
}
