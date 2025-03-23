package com.example.ssauc.user.bid.dto;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class AutoBidRequestDto {
    private Long productId;
    private String userId;
    private int maxBidAmount;
}
