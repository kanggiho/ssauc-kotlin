package com.example.ssauc.user.login.handler


import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import lombok.extern.slf4j.Slf4j
import org.springframework.security.core.Authentication
import org.springframework.util.StringUtils
import java.io.IOException

/**
 * OAuth2 로그인 성공 시 JWT Access Token 발급 후 "jwt_access" 쿠키에 설정
 */
@Slf4j
class OAuth2LoginSuccessHandler(jwtUtil: JwtUtil, refreshTokenService: RefreshTokenService) :
    AuthenticationSuccessHandler {
    private val jwtUtil: JwtUtil = jwtUtil
    private val refreshTokenService: RefreshTokenService = refreshTokenService

    @Throws(IOException::class)
    override fun onAuthenticationSuccess(
        request: HttpServletRequest?,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        if (authentication.getPrincipal() is OAuth2User) {
            val oAuth2User: OAuth2User = authentication.getPrincipal() as OAuth2User
            val email = oAuth2User.getAttributes().get("email") as String
            val profileImage = oAuth2User.getAttributes()
                .get("https://ssg-be-s3-bucket.s3.ap-northeast-2.amazonaws.com/default-profile.png") as String
            if (StringUtils.hasText(email)) {
                // 1) Access Token 생성
                val accessToken: String = jwtUtil.generateAccessToken(email)
                // 2) Refresh Token 생성
                val refreshToken: String = jwtUtil.generateRefreshToken(email)
                // 3) Redis에 저장
                refreshTokenService.saveRefreshToken(email, refreshToken)

                // 4) Access Token 쿠키 설정
                val accessCookie = Cookie("jwt_access", accessToken)
                accessCookie.isHttpOnly = true
                accessCookie.path = "/"
                response.addCookie(accessCookie)

                // 5) Refresh Token 쿠키 설정
                val refreshCookie = Cookie("jwt_refresh", refreshToken)
                refreshCookie.isHttpOnly = true
                refreshCookie.path = "/"
                response.addCookie(refreshCookie)

                OAuth2LoginSuccessHandler.log.info("소셜 로그인 성공: {}, Access/Refresh 토큰 생성 및 Redis 저장 완료", email)
            }
        }

        // 로그인 성공 후 메인 페이지로 리다이렉트
        response.sendRedirect("/")
    }
}
