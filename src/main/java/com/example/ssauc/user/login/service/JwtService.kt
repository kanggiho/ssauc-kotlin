package com.example.ssauc.user.login.service

import com.example.ssauc.user.login.entity.Users
import com.example.ssauc.user.login.repository.UsersRepository
import com.example.ssauc.user.login.util.JwtUtil
import jakarta.servlet.http.HttpServletRequest
import lombok.RequiredArgsConstructor
import lombok.extern.slf4j.Slf4j
import org.springframework.stereotype.Service


@Slf4j
@Service
@RequiredArgsConstructor
class JwtService {
    private val jwtUtil: JwtUtil? = null
    private val userRepository: UsersRepository? = null

    /**
     * 요청에서 Access Token을 추출하여 JWT 검증 후 해당 사용자 정보를 반환합니다.
     */
    fun extractUser(request: HttpServletRequest): Users? {
        val token = extractToken(request)
        if (token == null) {
            JwtService.log.warn("토큰 추출 실패")
            return null
        }
        val email = jwtUtil!!.validateAccessToken(token)
        JwtService.log.info("토큰에서 추출한 email: {}", email)
        if (email != null) {
            val userOpt = userRepository!!.findByEmail(email)
            if (userOpt!!.isPresent) {
                JwtService.log.info("DB에서 사용자 조회 성공: {}", email)
                return userOpt.get()
            } else {
                JwtService.log.warn("DB에서 사용자 조회 실패: {}", email)
            }
        } else {
            JwtService.log.warn("토큰 유효성 검증 실패")
        }
        return null
    }

    /**
     * Authorization 헤더 또는 쿠키 "jwt_access"에서 토큰을 추출합니다.
     */
    private fun extractToken(request: HttpServletRequest): String? {
        val auth = request.getHeader("Authorization")
        if (auth != null && auth.startsWith("Bearer ")) {
            return auth.substring(7)
        }
        if (request.cookies != null) {
            for (c in request.cookies) {
                if ("jwt_access" == c.name) {
                    return c.value
                }
            }
        }
        return null
    }
}
