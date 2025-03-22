package com.example.ssauc.user.login.handler;


import com.example.ssauc.user.login.service.RefreshTokenService;
import com.example.ssauc.user.login.util.JwtUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

@Slf4j
public class CustomLogoutSuccessHandler implements LogoutSuccessHandler {


    private final RefreshTokenService refreshTokenService;
    private final JwtUtil jwtUtil;

    public CustomLogoutSuccessHandler(RefreshTokenService refreshTokenService, JwtUtil jwtUtil) {
        this.refreshTokenService = refreshTokenService;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {

        String email = null;
        if (authentication != null) {
            email = authentication.getName();
            log.info("로그아웃 요청: authentication에서 email 추출 = {}", email);
        } else if (request.getCookies() != null) {
            Optional<Cookie> refreshCookieOpt = Arrays.stream(request.getCookies())
                    .filter(c -> "jwt_refresh".equals(c.getName()))
                    .findFirst();
            if (refreshCookieOpt.isPresent()) {
                String refreshToken = refreshCookieOpt.get().getValue();
                email = jwtUtil.validateRefreshToken(refreshToken);
                if (email != null) {
                    log.info("쿠키에서 추출한 refresh token으로 email 확인 = {}", email);
                } else {
                    log.warn("쿠키의 refresh token이 유효하지 않음");
                }
            } else {
                log.warn("refresh token 쿠키가 존재하지 않음");
            }
        } else {
            log.warn("요청에 쿠키가 없음");
        }

        if (email != null) {
            refreshTokenService.deleteRefreshToken(email);
        } else {
            log.warn("Refresh Token 삭제를 위한 email 정보 확보 실패");
        }

        // JWT 관련 쿠키 삭제
        Cookie accessCookie = new Cookie("jwt_access", null);
        accessCookie.setMaxAge(0);
        accessCookie.setPath("/");
        response.addCookie(accessCookie);

        Cookie refreshCookie = new Cookie("jwt_refresh", null);
        refreshCookie.setMaxAge(0);
        refreshCookie.setPath("/");
        response.addCookie(refreshCookie);

        response.sendRedirect("/login?logout=true");
    }
}
