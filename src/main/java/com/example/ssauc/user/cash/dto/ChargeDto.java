package com.example.ssauc.user.cash.dto;

import java.time.LocalDateTime;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChargeDto {
    private Long chargeId;
    private String chargeType;
    private Long amount;
    private String status;
    private LocalDateTime createdAt;
    private String receiptUrl;
}
