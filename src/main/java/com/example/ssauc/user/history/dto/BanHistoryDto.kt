package com.example.ssauc.user.history.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BanHistoryDto {
    private Long banId;
    private String blockedUserName;
    private String profileImage;
    private LocalDateTime blockedAt;
}
