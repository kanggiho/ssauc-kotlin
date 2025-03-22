package com.example.ssauc.user.login.security;

import com.example.ssauc.user.login.entity.Users;
import com.example.ssauc.user.login.repository.UsersRepository;
import com.example.ssauc.user.login.util.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UsersRepository userRepository;

    public JwtAuthenticationFilter(JwtUtil jwtUtil,
                                   UsersRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String token = extractToken(request);
        if (token != null) {
            try {
                // JWT 검증 후 이메일 추출
                String email = jwtUtil.validateAccessToken(token);
                if (email != null) {
                    // DB에서 해당 사용자의 전체 정보를 조회
                    Optional<Users> userOpt = userRepository.findByEmail(email);
                    if (userOpt.isPresent()) {
                        Users userEntity = userOpt.get();
                        // Authentication의 principal로 Users 엔티티를 설정
                        UsernamePasswordAuthenticationToken auth =
                                new UsernamePasswordAuthenticationToken(
                                        userEntity,
                                        null,
                                        Collections.singletonList(() -> "ROLE_USER")
                                );
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    }
                }
            } catch (ExpiredJwtException | SignatureException e) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "ACCESS TOKEN INVALID");
                return;
            } catch (Exception e) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "JWT ERROR");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    // Authorization 헤더 또는 쿠키 "jwt_access"에서 토큰을 추출
    private String extractToken(HttpServletRequest request) {
        String auth = request.getHeader("Authorization");
        if (StringUtils.hasText(auth) && auth.startsWith("Bearer ")) {
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