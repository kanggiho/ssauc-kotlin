package com.example.ssauc.user.login.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    private final Key accessKey;
    private final Key refreshKey;
    private final long accessExpireMs;
    private final long refreshExpireMs;

    public JwtUtil(
            @Value("${jwt.secret.access}") String accessSecret,
            @Value("${jwt.secret.refresh}") String refreshSecret,
            @Value("${jwt.expire.access}") long accessExpireMs,
            @Value("${jwt.expire.refresh}") long refreshExpireMs
    ) {
        this.accessKey = Keys.hmacShaKeyFor(accessSecret.getBytes(StandardCharsets.UTF_8));
        this.refreshKey = Keys.hmacShaKeyFor(refreshSecret.getBytes(StandardCharsets.UTF_8));
        this.accessExpireMs = accessExpireMs;
        this.refreshExpireMs = refreshExpireMs;
    }

    /** Access Token 생성 (예: 30분 유효) */
    public String generateAccessToken(String email) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + accessExpireMs))
                .signWith(accessKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /** Refresh Token 생성 (예: 2주 유효) */
    public String generateRefreshToken(String email) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + refreshExpireMs))
                .signWith(refreshKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /** Access Token 유효성 검사 → 유효하면 email 반환, 아니면 null */
    public String validateAccessToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(accessKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }

    /** Refresh Token 유효성 검사 → 유효하면 email 반환, 아니면 null */
    public String validateRefreshToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(refreshKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }

    /** JWT 토큰에서 사용자 이메일(또는 식별자))을 추출하는 메서드 */
    public String getUsernameFromToken(String token) {
        return validateAccessToken(token);
    }
}
