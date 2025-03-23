package com.example.ssauc.user.login.security

import io.jsonwebtoken.security.SignatureException
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.util.StringUtils
import java.io.IOException
import java.util.*

class JwtAuthenticationFilter(
    jwtUtil: JwtUtil,
    userRepository: UsersRepository
) : OncePerRequestFilter() {
    private val jwtUtil: JwtUtil = jwtUtil
    private val userRepository: UsersRepository = userRepository

    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val token = extractToken(request)
        if (token != null) {
            try {
                // JWT 검증 후 이메일 추출
                val email: String = jwtUtil.validateAccessToken(token)
                if (email != null) {
                    // DB에서 해당 사용자의 전체 정보를 조회
                    val userOpt: Optional<Users> = userRepository.findByEmail(email)
                    if (userOpt.isPresent()) {
                        val userEntity: Users = userOpt.get()
                        // Authentication의 principal로 Users 엔티티를 설정
                        val auth: UsernamePasswordAuthenticationToken =
                            UsernamePasswordAuthenticationToken(
                                userEntity,
                                null,
                                listOf<T>(T { "ROLE_USER" })
                            )
                        SecurityContextHolder.getContext().setAuthentication(auth)
                    }
                }
            } catch (e: ExpiredJwtException) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "ACCESS TOKEN INVALID")
                return
            } catch (e: SignatureException) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "ACCESS TOKEN INVALID")
                return
            } catch (e: Exception) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "JWT ERROR")
                return
            }
        }
        filterChain.doFilter(request, response)
    }

    // Authorization 헤더 또는 쿠키 "jwt_access"에서 토큰을 추출
    private fun extractToken(request: HttpServletRequest): String? {
        val auth = request.getHeader("Authorization")
        if (StringUtils.hasText(auth) && auth.startsWith("Bearer ")) {
            return auth.substring(7)
        }
        if (request.cookies != null) {
            for (c in request.cookies) {
                if ("jwt_access" == c.name) {
                    return c.value
                }
            }
        }
        return null
    }
}