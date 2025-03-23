package com.example.ssauc.user.bid.dto

import lombok.*

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
class BidRequestDto {
    var productId: Long? = null
    var userId: String? = null
    var bidAmount: Int = 0
}
