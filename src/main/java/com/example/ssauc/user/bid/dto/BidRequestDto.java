package com.example.ssauc.user.bid.dto;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class BidRequestDto {
    private Long productId;
    private String userId;
    private int bidAmount;
}
