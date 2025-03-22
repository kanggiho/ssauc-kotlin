package com.example.ssauc.user.login.util;

import com.example.ssauc.user.login.entity.Users;
import com.example.ssauc.user.login.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


// HTTP 요청 헤더 또는 쿠키에서 JWT 토큰을 추출
@Component
@RequiredArgsConstructor
public class TokenExtractor {

    private final JwtUtil jwtUtil;
    private final UserService userService;

    public Users getUserFromToken(HttpServletRequest request) {
        String token = null;
        // 헤더의 Authorization에서 "Bearer " 접두사가 붙은 토큰 추출
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            token = bearerToken.substring(7);
        } else if (request.getCookies() != null) {
            // 쿠키에서 "jwt_access" 이름의 토큰 추출
            for (Cookie cookie : request.getCookies()) {
                if ("jwt_access".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }
        if (token == null || token.isEmpty()) {
            return null;
        }
        // 토큰에서 이메일(사용자 식별자)을 추출 후 DB에서 사용자 정보 조회
        String email = jwtUtil.getUsernameFromToken(token);
        return userService.getCurrentUser(email);
    }
}