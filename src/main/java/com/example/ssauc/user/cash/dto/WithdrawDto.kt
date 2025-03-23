package com.example.ssauc.user.cash.dto;

import java.time.LocalDateTime;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WithdrawDto {
    private Long withdrawId;
    private String bank;
    private String account;
    private Long netAmount; // amount - commission
    private LocalDateTime withdrawAt;
    private String requestStatus; // withdrawAt != null ? "완료" : "처리중"
}
