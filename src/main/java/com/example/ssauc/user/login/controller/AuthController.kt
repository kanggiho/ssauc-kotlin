package com.example.ssauc.user.login.controller;

import com.example.ssauc.user.login.dto.LoginResponseDTO;
import com.example.ssauc.user.login.dto.UserRegistrationDTO;
import com.example.ssauc.user.login.service.RefreshTokenService;
import com.example.ssauc.user.login.service.UserService;

import com.example.ssauc.user.login.util.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService; // 주입 추가

    @GetMapping("/signup")
    public String signupForm() {
        // 회원가입 폼 (Thymeleaf 템플릿: templates/login/signup.html)
        return "login/signup";
    }

    @PostMapping("/signup")
    public String register(UserRegistrationDTO dto) {
        String result = userService.register(dto);
        if ("회원가입 성공".equals(result)) {
            return "redirect:/login?success=true";
        }
        return "redirect:/signup?error=" + result;
    }

    @GetMapping("/login")
    public String loginForm() {
        // 로그인 폼 (Thymeleaf 템플릿: templates/login/login.html)
        return "login/login";
    }

    @PostMapping("/login")
    public String doLogin(@RequestParam String email,
                          @RequestParam String password,
                          HttpServletResponse response) {
        log.info("로그인 요청 - email: {}, password: {}", email, password);
        Optional<LoginResponseDTO> loginResponseOpt = userService.login(email, password);
        if (loginResponseOpt.isPresent()) {
            LoginResponseDTO loginResponse = loginResponseOpt.get();

            // JWT 토큰 쿠키 설정
            Cookie accessCookie = new Cookie("jwt_access", loginResponse.getAccessToken());
            accessCookie.setHttpOnly(true);
            accessCookie.setPath("/");
            response.addCookie(accessCookie);

            Cookie refreshCookie = new Cookie("jwt_refresh", loginResponse.getRefreshToken());
            refreshCookie.setHttpOnly(true);
            refreshCookie.setPath("/");
            response.addCookie(refreshCookie);

            log.info("로그인 성공: {} -> jwt_access, jwt_refresh 쿠키 발급됨", email);

            // (추가) SecurityContext에 인증 정보 설정
            List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(email, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authToken);
            log.info("SecurityContext에 직접 인증 정보 설정: {}", email);

            return "redirect:/";
        }
        // 로그인 실패 시 로그인 페이지로 리다이렉트 (리다이렉트 시 URL에 error=true를 붙여 에러 메시지를 표시)
        return "redirect:/login?error=true&email=" + java.net.URLEncoder.encode(email, java.nio.charset.StandardCharsets.UTF_8);
    }

}
