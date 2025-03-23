package com.example.ssauc.user.login.service;


import com.example.ssauc.user.login.entity.Users;
import com.example.ssauc.user.login.repository.UsersRepository;
import com.example.ssauc.user.login.util.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtUtil jwtUtil;
    private final UsersRepository userRepository;

    /**
     * 요청에서 Access Token을 추출하여 JWT 검증 후 해당 사용자 정보를 반환합니다.
     */
    public Users extractUser(HttpServletRequest request) {
        String token = extractToken(request);
        if (token == null) {
            log.warn("토큰 추출 실패");
            return null;
        }
        String email = jwtUtil.validateAccessToken(token);
        log.info("토큰에서 추출한 email: {}", email);
        if (email != null) {
            Optional<Users> userOpt = userRepository.findByEmail(email);
            if (userOpt.isPresent()) {
                log.info("DB에서 사용자 조회 성공: {}", email);
                return userOpt.get();
            } else {
                log.warn("DB에서 사용자 조회 실패: {}", email);
            }
        } else {
            log.warn("토큰 유효성 검증 실패");
        }
        return null;
    }

    /**
     * Authorization 헤더 또는 쿠키 "jwt_access"에서 토큰을 추출합니다.
     */
    private String extractToken(HttpServletRequest request) {
        String auth = request.getHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            return auth.substring(7);
        }
        if (request.getCookies() != null) {
            for (Cookie c : request.getCookies()) {
                if ("jwt_access".equals(c.getName())) {
                    return c.getValue();
                }
            }
        }
        return null;
    }
}
