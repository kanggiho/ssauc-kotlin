package com.example.ssauc.user.login.handler;


import com.example.ssauc.user.login.service.RefreshTokenService;
import com.example.ssauc.user.login.util.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.util.StringUtils;

import java.io.IOException;

/**
 * OAuth2 로그인 성공 시 JWT Access Token 발급 후 "jwt_access" 쿠키에 설정
 */
@Slf4j
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    public OAuth2LoginSuccessHandler(JwtUtil jwtUtil, RefreshTokenService refreshTokenService) {
        this.jwtUtil = jwtUtil;
        this.refreshTokenService = refreshTokenService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        if (authentication.getPrincipal() instanceof OAuth2User) {
            OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
            String email = (String) oAuth2User.getAttributes().get("email");
            String profileImage = (String) oAuth2User.getAttributes().get("https://ssg-be-s3-bucket.s3.ap-northeast-2.amazonaws.com/default-profile.png");
            if (StringUtils.hasText(email)) {
                // 1) Access Token 생성
                String accessToken = jwtUtil.generateAccessToken(email);
                // 2) Refresh Token 생성
                String refreshToken = jwtUtil.generateRefreshToken(email);
                // 3) Redis에 저장
                refreshTokenService.saveRefreshToken(email, refreshToken);

                // 4) Access Token 쿠키 설정
                Cookie accessCookie = new Cookie("jwt_access", accessToken);
                accessCookie.setHttpOnly(true);
                accessCookie.setPath("/");
                response.addCookie(accessCookie);

                // 5) Refresh Token 쿠키 설정
                Cookie refreshCookie = new Cookie("jwt_refresh", refreshToken);
                refreshCookie.setHttpOnly(true);
                refreshCookie.setPath("/");
                response.addCookie(refreshCookie);

                log.info("소셜 로그인 성공: {}, Access/Refresh 토큰 생성 및 Redis 저장 완료", email);
            }
        }

        // 로그인 성공 후 메인 페이지로 리다이렉트
        response.sendRedirect("/");
    }
}
