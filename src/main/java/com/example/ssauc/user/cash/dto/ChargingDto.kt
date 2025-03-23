package com.example.ssauc.user.cash.dto

import lombok.*

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
class ChargingDto {
    private val id: String? = null
    private val name: String? = null
    private val price: Long = 0
    private val currency: String? = null
}
