package com.example.ssauc.user.login.dto

import lombok.AllArgsConstructor
import lombok.Getter

@Getter
@AllArgsConstructor
class LoginResponseDTO {
    private val accessToken: String? = null
    private val refreshToken: String? = null
}
