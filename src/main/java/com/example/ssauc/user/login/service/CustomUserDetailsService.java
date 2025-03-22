package com.example.ssauc.user.login.service;

import com.example.ssauc.user.login.entity.Users;
import com.example.ssauc.user.login.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UsersRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) {
        Users user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        // 익명 내부 클래스로 UserDetails 구현. getUsername()은 로그인 후 헤더에서 닉네임으로 표시되도록 user.getUserName() 반환
        return new UserDetails() {
            @Override
            public Collection<? extends GrantedAuthority> getAuthorities() {
                // 기본적으로 ROLE_USER 권한만 반환합니다.
                return Collections.singleton(() -> "ROLE_USER");
            }

            @Override
            public String getPassword() {
                return user.getPassword();
            }

            // 여기서 이메일이 아니라 닉네임을 반환하여, 로그인 후 authentication.name이 닉네임이 되도록 함
            @Override
            public String getUsername() {
                return user.getUserName();
            }

            @Override
            public boolean isAccountNonExpired() {
                return true;
            }

            @Override
            public boolean isAccountNonLocked() {
                return true;
            }

            @Override
            public boolean isCredentialsNonExpired() {
                return true;
            }

            @Override
            public boolean isEnabled() {
                // 계정 상태가 "active"인 경우에만 사용 가능하도록 설정
                return "active".equalsIgnoreCase(user.getStatus());
            }
        };
    }
}
