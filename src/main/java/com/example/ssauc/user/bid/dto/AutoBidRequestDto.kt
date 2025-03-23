package com.example.ssauc.user.bid.dto

import lombok.*

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
class AutoBidRequestDto {
    public val productId: Long? = null
    public val userId: String? = null
    public val maxBidAmount = 0
}
