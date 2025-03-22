package com.example.ssauc.user.cash.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChargingDto {
    private String id;
    private String name;
    private long price;
    private String currency;
}
