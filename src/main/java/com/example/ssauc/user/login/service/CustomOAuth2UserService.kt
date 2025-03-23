package com.example.ssauc.user.login.service

import com.example.ssauc.user.login.repository.UsersRepository
import lombok.RequiredArgsConstructor
import org.springframework.context.annotation.Lazy
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.oauth2.core.OAuth2Error
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service

@Service
@RequiredArgsConstructor
@Lazy
class CustomOAuth2UserService : DefaultOAuth2UserService() {
    private val usersRepository: UsersRepository? = null

    @Lazy
    private val passwordEncoder: PasswordEncoder? = null

    @Throws(OAuth2AuthenticationException::class)
    override fun loadUser(userRequest: OAuth2UserRequest): OAuth2User {
        val oauth2User = super.loadUser(userRequest)
        return processOAuth2User(userRequest, oauth2User)
    }

    private fun processOAuth2User(userRequest: OAuth2UserRequest, oauth2User: OAuth2User): OAuth2User {
        val registrationId = userRequest.clientRegistration.registrationId
        val attributes = oauth2User.attributes

        var email: String? = null
        var nickname: String? = null

        if ("google" == registrationId) {
            email = attributes["email"] as String?
            nickname = attributes["name"] as String?
        } else if ("naver" == registrationId) {
            val response = attributes["response"] as Map<String, Any>?
            if (response != null) {
                email = response["email"] as String?
                nickname = response["name"] as String?
            }
        } else if ("kakao" == registrationId) {
            val kakaoId = attributes["id"].toString()
            val kakaoAccount = attributes["kakao_account"] as Map<String, Any>?
            if (kakaoAccount != null) {
                email = kakaoAccount["email"] as String?
                val profile = kakaoAccount["profile"] as Map<String, Any>?
                if (profile != null) {
                    nickname = profile["nickname"] as String?
                }
            }
            if (email == null) {
                email = "$kakaoId@kakao.com"
            }
            if (nickname == null) {
                nickname = "KakaoUser_$kakaoId"
            }
        }

        if (email == null) {
            throw OAuth2AuthenticationException(OAuth2Error("invalid_request", "이메일을 가져올 수 없습니다.", null))
        }
        if (nickname == null) {
            nickname = "SocialUser"
        }

        val userOptional = usersRepository!!.findByEmail(email)
        if (userOptional!!.isEmpty) {
            // DB에 없는 사용자이면 additional_info_required
            val oauth2Error = OAuth2Error(
                "additional_info_required",
                "additional_info_required:$email:$nickname",
                null
            )
            throw OAuth2AuthenticationException(oauth2Error, oauth2Error.description)
        }

        // user가 존재하지만, status != active이면 소셜 로그인 불가
        val user = userOptional.get()
        if (!"active".equals(user.status, ignoreCase = true)) {
            val oauth2Error = OAuth2Error(
                "account_inactive",
                "inactive|$email|$nickname",  // inactive|이메일|닉네임
                null
            )
            throw OAuth2AuthenticationException(oauth2Error, "inactive user")
        }

        // active 계정이면 로그인 허용
        val modifiableAttributes: MutableMap<String, Any> = HashMap(attributes)
        modifiableAttributes["email"] = email

        return DefaultOAuth2User(
            setOf<org.springframework.security.core.GrantedAuthority>(org.springframework.security.core.GrantedAuthority { "ROLE_USER" }),
            modifiableAttributes,
            "email"
        )
    }
}
