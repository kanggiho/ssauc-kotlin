package com.example.ssauc.user.chat.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BanRequestDto {
    private Long userId;         // 차단하는 사용자 ID
    private Long blockedUserId;  // 차단 당하는 사용자 ID
}
