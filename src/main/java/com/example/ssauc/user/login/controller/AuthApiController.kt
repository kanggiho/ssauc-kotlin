package com.example.ssauc.user.login.controller;


import com.example.ssauc.user.login.service.RefreshTokenService;
import com.example.ssauc.user.login.util.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthApiController {

    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    /**
     * 리프레시 토큰으로 Access Token 재발급
     * 실제 구현에서는 Refresh Token은 Redis에 저장되어 있는 값을 확인하도록 함.
     */
    @PostMapping("/refresh-token")
    public String refreshToken(@RequestParam String refreshToken, HttpServletResponse response) {
        String email = jwtUtil.validateRefreshToken(refreshToken);
        if (email == null) {
            return "리프레시 토큰이 유효하지 않습니다.";
        }
        String storedToken = refreshTokenService.getRefreshToken(email);
        if (!refreshToken.equals(storedToken)) {
            return "리프레시 토큰이 만료되었거나 일치하지 않습니다.";
        }
        // 새 Access Token 발급 (예: 쿠키에 담아서 전송)
        String newAccessToken = jwtUtil.generateAccessToken(email);
        Cookie jwtCookie = new Cookie("jwt_access", newAccessToken);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setPath("/");
        response.addCookie(jwtCookie);
        return "새 Access Token 발급: " + email;
    }
}
