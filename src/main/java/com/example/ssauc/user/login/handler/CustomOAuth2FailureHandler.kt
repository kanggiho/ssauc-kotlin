package com.example.ssauc.user.login.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class CustomOAuth2FailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException, ServletException {
        log.warn("소셜 로그인 실패: {}", exception.getMessage());

        if (exception instanceof OAuth2AuthenticationException oauthEx) {
            OAuth2Error error = oauthEx.getError();
            String desc = error.getDescription(); // ex: "inactive|email@domain.com|닉네임"
            if (desc != null && desc.startsWith("inactive|")) {
                String[] parts = desc.split("\\|");
                if (parts.length >= 3) {
                    String email = parts[1];
                    String nickname = parts[2];
                    // URL 인코딩
                    String encodedEmail = URLEncoder.encode(email, StandardCharsets.UTF_8);
                    String encodedNickname = URLEncoder.encode(nickname, StandardCharsets.UTF_8);
                    // signup 페이지로 이동 (이메일/닉네임 넘김)
                    String redirectUrl = "/signup?inactive=true&email=" + encodedEmail + "&nickname=" + encodedNickname;
                    response.sendRedirect(redirectUrl);
                    return;
                }
            } else if (desc != null && desc.startsWith("additional_info_required:")) {
                // DB에 없는 사용자 -> 추가 정보 받아서 회원가입
                String[] parts = desc.split(":");
                if (parts.length >= 3) {
                    String email = parts[1];
                    String nickname = parts[2];
                    String encodedEmail = URLEncoder.encode(email, StandardCharsets.UTF_8);
                    String encodedNickname = URLEncoder.encode(nickname, StandardCharsets.UTF_8);
                    String redirectUrl = "/signup?email=" + encodedEmail + "&nickname=" + encodedNickname;
                    response.sendRedirect(redirectUrl);
                    return;
                }
            }
        }

        // 그 외 일반적인 실패 처리
        response.sendRedirect("/login?error=true");
    }
}
