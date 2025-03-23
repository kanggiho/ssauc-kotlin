package com.example.ssauc.user.login.config

import org.springframework.security.authentication.AuthenticationManager

@org.springframework.context.annotation.Configuration
@EnableWebSecurity
@lombok.RequiredArgsConstructor
class SecurityConfig {
    private val jwtUtil: JwtUtil? = null
    private val customOAuth2FailureHandler: CustomOAuth2FailureHandler? = null

    @org.springframework.context.annotation.Lazy
    private val customOAuth2UserService: CustomOAuth2UserService? = null
    private val usersRepository: UsersRepository? = null

    @org.springframework.context.annotation.Bean
    fun jwtAuthenticationFilter(): JwtAuthenticationFilter {
        return JwtAuthenticationFilter(jwtUtil, usersRepository)
    }

    @org.springframework.context.annotation.Bean
    fun oAuth2LoginSuccessHandler(refreshTokenService: RefreshTokenService?): OAuth2LoginSuccessHandler {
        return OAuth2LoginSuccessHandler(jwtUtil, refreshTokenService)
    }

    @org.springframework.context.annotation.Bean
    @Throws(java.lang.Exception::class)
    fun securityFilterChain(http: HttpSecurity, refreshTokenService: RefreshTokenService?): SecurityFilterChain {
        http
            .sessionManagement(org.springframework.security.config.Customizer<SessionManagementConfigurer<HttpSecurity?>> { sm: SessionManagementConfigurer<HttpSecurity?> ->
                sm.sessionCreationPolicy(
                    SessionCreationPolicy.STATELESS
                )
            })
            .csrf(org.springframework.security.config.Customizer<CsrfConfigurer<HttpSecurity?>> { csrf: CsrfConfigurer<HttpSecurity?> -> csrf.disable() })
            .authorizeHttpRequests(org.springframework.security.config.Customizer<AuthorizationManagerRequestMatcherRegistry> { auth: AuthorizationManagerRequestMatcherRegistry ->
                auth
                    .requestMatchers(
                        "/**",
                        "/login",
                        "/signup",
                        "/register",
                        "/api/user/**",
                        "/api/auth/refresh-token",
                        "/oauth2/**",
                        "/api/find-id/**",
                        "/api/reset-password/**",
                        "/css/**",
                        "/js/**",
                        "/img/**",
                        "/admin/**",
                        "/list/**"
                    ).permitAll()
                    .anyRequest().authenticated()
            }
            )
            .oauth2Login(org.springframework.security.config.Customizer<OAuth2LoginConfigurer<HttpSecurity?>> { oauth2: OAuth2LoginConfigurer<HttpSecurity?> ->
                oauth2
                    .loginPage("/login")
                    .userInfoEndpoint(org.springframework.security.config.Customizer<UserInfoEndpointConfig> { userInfo: UserInfoEndpointConfig ->
                        userInfo.userService(
                            customOAuth2UserService
                        )
                    })
                    .successHandler(oAuth2LoginSuccessHandler(refreshTokenService))
                    .failureHandler(customOAuth2FailureHandler)
            }
            )
            .formLogin(org.springframework.security.config.Customizer<FormLoginConfigurer<HttpSecurity?>> { form: FormLoginConfigurer<HttpSecurity?> -> form.disable() })
            .logout(org.springframework.security.config.Customizer<LogoutConfigurer<HttpSecurity?>> { logout: LogoutConfigurer<HttpSecurity?> ->
                logout
                    .logoutUrl("/logout")
                    .logoutSuccessHandler(CustomLogoutSuccessHandler(refreshTokenService, jwtUtil))
                    .invalidateHttpSession(true)
                    .deleteCookies("JSESSIONID", "jwt_access", "jwt_refresh")
            }
            )
            .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }

    @org.springframework.context.annotation.Bean
    @Throws(java.lang.Exception::class)
    fun authenticationManager(configuration: AuthenticationConfiguration): AuthenticationManager {
        return configuration.getAuthenticationManager()
    }
}
