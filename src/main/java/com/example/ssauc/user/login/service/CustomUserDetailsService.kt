package com.example.ssauc.user.login.service

import org.springframework.security.core.GrantedAuthority

@org.springframework.stereotype.Service
@lombok.RequiredArgsConstructor
class CustomUserDetailsService : UserDetailsService {
    private val userRepository: UsersRepository? = null

    override fun loadUserByUsername(email: String): UserDetails {
        val user: Users = userRepository.findByEmail(email)
            .orElseThrow<java.lang.RuntimeException>(java.util.function.Supplier<java.lang.RuntimeException> {
                UsernameNotFoundException(
                    "User not found with email: $email"
                )
            })
        // 익명 내부 클래스로 UserDetails 구현. getUsername()은 로그인 후 헤더에서 닉네임으로 표시되도록 user.getUserName() 반환
        return object : UserDetails() {
            val authorities: Collection<Any?>
                get() =// 기본적으로 ROLE_USER 권한만 반환합니다.
                    setOf<GrantedAuthority>(GrantedAuthority { "ROLE_USER" })

            val password: String
                get() = user.password

            val username: String
                // 여기서 이메일이 아니라 닉네임을 반환하여, 로그인 후 authentication.name이 닉네임이 되도록 함
                get() = user.userName

            val isAccountNonExpired: Boolean
                get() = true

            val isAccountNonLocked: Boolean
                get() = true

            val isCredentialsNonExpired: Boolean
                get() = true

            val isEnabled: Boolean
                get() =// 계정 상태가 "active"인 경우에만 사용 가능하도록 설정
                    "active".equals(user.status, ignoreCase = true)
        }
    }
}
