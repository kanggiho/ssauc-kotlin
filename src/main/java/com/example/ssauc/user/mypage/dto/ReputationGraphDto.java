package com.example.ssauc.user.mypage.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ReputationGraphDto {
    // 평판 그래프 데이터 전달용
    private LocalDateTime createdAt;
    private Double newScore;
}
