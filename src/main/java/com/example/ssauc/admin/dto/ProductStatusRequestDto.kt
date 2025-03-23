package com.example.ssauc.admin.dto

import lombok.*

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
class ProductStatusRequestDto {
    public val productId: Long? = null
    public val status: String? = null
}
