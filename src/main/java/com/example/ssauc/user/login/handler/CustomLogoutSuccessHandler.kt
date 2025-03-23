package com.example.ssauc.user.login.handler


import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import lombok.extern.slf4j.Slf4j
import org.springframework.security.core.Authentication
import java.io.IOException
import java.util.*

@Slf4j
class CustomLogoutSuccessHandler(refreshTokenService: RefreshTokenService, jwtUtil: JwtUtil) : LogoutSuccessHandler {
    private val refreshTokenService: RefreshTokenService = refreshTokenService
    private val jwtUtil: JwtUtil = jwtUtil

    @Throws(IOException::class, ServletException::class)
    override fun onLogoutSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication?
    ) {
        var email: String? = null
        if (authentication != null) {
            email = authentication.getName()
            CustomLogoutSuccessHandler.log.info("로그아웃 요청: authentication에서 email 추출 = {}", email)
        } else if (request.cookies != null) {
            val refreshCookieOpt = Arrays.stream(request.cookies)
                .filter { c: Cookie -> "jwt_refresh" == c.name }
                .findFirst()
            if (refreshCookieOpt.isPresent) {
                val refreshToken = refreshCookieOpt.get().value
                email = jwtUtil.validateRefreshToken(refreshToken)
                if (email != null) {
                    CustomLogoutSuccessHandler.log.info("쿠키에서 추출한 refresh token으로 email 확인 = {}", email)
                } else {
                    CustomLogoutSuccessHandler.log.warn("쿠키의 refresh token이 유효하지 않음")
                }
            } else {
                CustomLogoutSuccessHandler.log.warn("refresh token 쿠키가 존재하지 않음")
            }
        } else {
            CustomLogoutSuccessHandler.log.warn("요청에 쿠키가 없음")
        }

        if (email != null) {
            refreshTokenService.deleteRefreshToken(email)
        } else {
            CustomLogoutSuccessHandler.log.warn("Refresh Token 삭제를 위한 email 정보 확보 실패")
        }

        // JWT 관련 쿠키 삭제
        val accessCookie = Cookie("jwt_access", null)
        accessCookie.maxAge = 0
        accessCookie.path = "/"
        response.addCookie(accessCookie)

        val refreshCookie = Cookie("jwt_refresh", null)
        refreshCookie.maxAge = 0
        refreshCookie.path = "/"
        response.addCookie(refreshCookie)

        response.sendRedirect("/login?logout=true")
    }
}
