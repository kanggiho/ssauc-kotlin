package com.example.ssauc.user.login.controller

import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletResponse
import lombok.RequiredArgsConstructor
import lombok.extern.slf4j.Slf4j
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.stereotype.Controller
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.*

@Slf4j
@Controller
@RequiredArgsConstructor
class AuthController {
    private val userService: UserService? = null
    private val jwtUtil: JwtUtil? = null
    private val refreshTokenService: RefreshTokenService? = null // 주입 추가

    @GetMapping("/signup")
    fun signupForm(): String {
        // 회원가입 폼 (Thymeleaf 템플릿: templates/login/signup.html)
        return "login/signup"
    }

    @PostMapping("/signup")
    fun register(dto: UserRegistrationDTO): String {
        val result: String = userService.register(dto)
        if ("회원가입 성공" == result) {
            return "redirect:/login?success=true"
        }
        return "redirect:/signup?error=$result"
    }

    @GetMapping("/login")
    fun loginForm(): String {
        // 로그인 폼 (Thymeleaf 템플릿: templates/login/login.html)
        return "login/login"
    }

    @PostMapping("/login")
    fun doLogin(
        @RequestParam email: String,
        @RequestParam password: String?,
        response: HttpServletResponse
    ): String {
        AuthController.log.info("로그인 요청 - email: {}, password: {}", email, password)
        val loginResponseOpt: Optional<LoginResponseDTO> = userService.login(email, password)
        if (loginResponseOpt.isPresent()) {
            val loginResponse: LoginResponseDTO = loginResponseOpt.get()

            // JWT 토큰 쿠키 설정
            val accessCookie = Cookie("jwt_access", loginResponse.getAccessToken())
            accessCookie.isHttpOnly = true
            accessCookie.path = "/"
            response.addCookie(accessCookie)

            val refreshCookie = Cookie("jwt_refresh", loginResponse.getRefreshToken())
            refreshCookie.isHttpOnly = true
            refreshCookie.path = "/"
            response.addCookie(refreshCookie)

            AuthController.log.info("로그인 성공: {} -> jwt_access, jwt_refresh 쿠키 발급됨", email)

            // (추가) SecurityContext에 인증 정보 설정
            val authorities: List<GrantedAuthority> = listOf<GrantedAuthority>(SimpleGrantedAuthority("ROLE_USER"))
            val authToken: UsernamePasswordAuthenticationToken =
                UsernamePasswordAuthenticationToken(email, null, authorities)
            SecurityContextHolder.getContext().setAuthentication(authToken)
            AuthController.log.info("SecurityContext에 직접 인증 정보 설정: {}", email)

            return "redirect:/"
        }
        // 로그인 실패 시 로그인 페이지로 리다이렉트 (리다이렉트 시 URL에 error=true를 붙여 에러 메시지를 표시)
        return "redirect:/login?error=true&email=" + URLEncoder.encode(email, StandardCharsets.UTF_8)
    }
}
