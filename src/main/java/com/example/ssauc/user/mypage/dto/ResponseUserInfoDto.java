package com.example.ssauc.user.mypage.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResponseUserInfoDto {
    private String userName;
    private String profileImage;
    private Double reputation;
    private String location;
    private String createdAt;
    private String lastLogin;
    private String reviewSummary;
}
