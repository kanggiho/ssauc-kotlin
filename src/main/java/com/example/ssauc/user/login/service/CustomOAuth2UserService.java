package com.example.ssauc.user.login.service;

import com.example.ssauc.user.login.entity.Users;
import com.example.ssauc.user.login.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Lazy
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UsersRepository usersRepository;
    private final @Lazy PasswordEncoder passwordEncoder;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);
        return processOAuth2User(userRequest, oauth2User);
    }

    @SuppressWarnings("unchecked")
    private OAuth2User processOAuth2User(OAuth2UserRequest userRequest, OAuth2User oauth2User) {
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        Map<String, Object> attributes = oauth2User.getAttributes();

        String email = null;
        String nickname = null;

        if ("google".equals(registrationId)) {
            email = (String) attributes.get("email");
            nickname = (String) attributes.get("name");
        } else if ("naver".equals(registrationId)) {
            Map<String, Object> response = (Map<String, Object>) attributes.get("response");
            if (response != null) {
                email = (String) response.get("email");
                nickname = (String) response.get("name");
            }
        } else if ("kakao".equals(registrationId)) {
            String kakaoId = String.valueOf(attributes.get("id"));
            Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
            if (kakaoAccount != null) {
                email = (String) kakaoAccount.get("email");
                Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
                if (profile != null) {
                    nickname = (String) profile.get("nickname");
                }
            }
            if (email == null) {
                email = kakaoId + "@kakao.com";
            }
            if (nickname == null) {
                nickname = "KakaoUser_" + kakaoId;
            }
        }

        if (email == null) {
            throw new OAuth2AuthenticationException(new OAuth2Error("invalid_request", "이메일을 가져올 수 없습니다.", null));
        }
        if (nickname == null) {
            nickname = "SocialUser";
        }

        Optional<Users> userOptional = usersRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            // DB에 없는 사용자이면 additional_info_required
            OAuth2Error oauth2Error = new OAuth2Error(
                    "additional_info_required",
                    "additional_info_required:" + email + ":" + nickname,
                    null
            );
            throw new OAuth2AuthenticationException(oauth2Error, oauth2Error.getDescription());
        }

        // user가 존재하지만, status != active이면 소셜 로그인 불가
        Users user = userOptional.get();
        if (!"active".equalsIgnoreCase(user.getStatus())) {
            OAuth2Error oauth2Error = new OAuth2Error(
                    "account_inactive",
                    "inactive|" + email + "|" + nickname,  // inactive|이메일|닉네임
                    null
            );
            throw new OAuth2AuthenticationException(oauth2Error, "inactive user");
        }

        // active 계정이면 로그인 허용
        Map<String, Object> modifiableAttributes = new HashMap<>(attributes);
        modifiableAttributes.put("email", email);

        return new DefaultOAuth2User(
                Collections.singleton(() -> "ROLE_USER"),
                modifiableAttributes,
                "email"
        );
    }
}
