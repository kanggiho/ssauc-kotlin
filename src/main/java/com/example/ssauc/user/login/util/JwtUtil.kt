package com.example.ssauc.user.login.util

import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.security.Key
import java.util.*

@Component
class JwtUtil(
    @Value("\${jwt.secret.access}") accessSecret: String,
    @Value("\${jwt.secret.refresh}") refreshSecret: String,
    @param:Value("\${jwt.expire.access}") private val accessExpireMs: Long,
    @param:Value("\${jwt.expire.refresh}") private val refreshExpireMs: Long
) {
    private val accessKey: Key = Keys.hmacShaKeyFor(accessSecret.toByteArray(StandardCharsets.UTF_8))
    private val refreshKey: Key = Keys.hmacShaKeyFor(refreshSecret.toByteArray(StandardCharsets.UTF_8))

    /** Access Token 생성 (예: 30분 유효)  */
    fun generateAccessToken(email: String?): String {
        val now = System.currentTimeMillis()
        return Jwts.builder()
            .setSubject(email)
            .setIssuedAt(Date(now))
            .setExpiration(Date(now + accessExpireMs))
            .signWith(accessKey, SignatureAlgorithm.HS256)
            .compact()
    }

    /** Refresh Token 생성 (예: 2주 유효)  */
    fun generateRefreshToken(email: String?): String {
        val now = System.currentTimeMillis()
        return Jwts.builder()
            .setSubject(email)
            .setIssuedAt(Date(now))
            .setExpiration(Date(now + refreshExpireMs))
            .signWith(refreshKey, SignatureAlgorithm.HS256)
            .compact()
    }

    /** Access Token 유효성 검사 → 유효하면 email 반환, 아니면 null  */
    fun validateAccessToken(token: String?): String? {
        return try {
            Jwts.parserBuilder()
                .setSigningKey(accessKey)
                .build()
                .parseClaimsJws(token)
                .body
                .subject
        } catch (e: JwtException) {
            null
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    /** Refresh Token 유효성 검사 → 유효하면 email 반환, 아니면 null  */
    fun validateRefreshToken(token: String?): String? {
        return try {
            Jwts.parserBuilder()
                .setSigningKey(refreshKey)
                .build()
                .parseClaimsJws(token)
                .body
                .subject
        } catch (e: JwtException) {
            null
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    /** JWT 토큰에서 사용자 이메일(또는 식별자))을 추출하는 메서드  */
    fun getUsernameFromToken(token: String?): String? {
        return validateAccessToken(token)
    }
}
