package com.example.ssauc.user.login.handler

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import lombok.extern.slf4j.Slf4j
import org.springframework.security.core.AuthenticationException
import org.springframework.security.oauth2.core.OAuth2Error
import org.springframework.stereotype.Component
import java.io.IOException
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Slf4j
@Component
class CustomOAuth2FailureHandler : SimpleUrlAuthenticationFailureHandler() {
    @Throws(IOException::class, ServletException::class)
    override fun onAuthenticationFailure(
        request: HttpServletRequest?,
        response: HttpServletResponse,
        exception: AuthenticationException
    ) {
        CustomOAuth2FailureHandler.log.warn("소셜 로그인 실패: {}", exception.getMessage())

        if (exception is OAuth2AuthenticationException) {
            val error: OAuth2Error = exception.getError()
            val desc = error.description // ex: "inactive|email@domain.com|닉네임"
            if (desc != null && desc.startsWith("inactive|")) {
                val parts = desc.split("\\|".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                if (parts.size >= 3) {
                    val email = parts[1]
                    val nickname = parts[2]
                    // URL 인코딩
                    val encodedEmail = URLEncoder.encode(email, StandardCharsets.UTF_8)
                    val encodedNickname = URLEncoder.encode(nickname, StandardCharsets.UTF_8)
                    // signup 페이지로 이동 (이메일/닉네임 넘김)
                    val redirectUrl =
                        "/signup?inactive=true&email=$encodedEmail&nickname=$encodedNickname"
                    response.sendRedirect(redirectUrl)
                    return
                }
            } else if (desc != null && desc.startsWith("additional_info_required:")) {
                // DB에 없는 사용자 -> 추가 정보 받아서 회원가입
                val parts = desc.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                if (parts.size >= 3) {
                    val email = parts[1]
                    val nickname = parts[2]
                    val encodedEmail = URLEncoder.encode(email, StandardCharsets.UTF_8)
                    val encodedNickname = URLEncoder.encode(nickname, StandardCharsets.UTF_8)
                    val redirectUrl = "/signup?email=$encodedEmail&nickname=$encodedNickname"
                    response.sendRedirect(redirectUrl)
                    return
                }
            }
        }

        // 그 외 일반적인 실패 처리
        response.sendRedirect("/login?error=true")
    }
}
