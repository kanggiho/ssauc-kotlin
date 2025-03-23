package com.example.ssauc.admin.dto

import lombok.*

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
class UsersStatusRequestDto {
    public val userId: Long? = null
    public val status: String? = null
}
