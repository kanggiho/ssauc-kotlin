package com.example.ssauc.user.login.config;

import com.example.ssauc.user.login.handler.CustomLogoutSuccessHandler;
import com.example.ssauc.user.login.handler.CustomOAuth2FailureHandler;
import com.example.ssauc.user.login.repository.UsersRepository;
import com.example.ssauc.user.login.security.JwtAuthenticationFilter;
import com.example.ssauc.user.login.handler.OAuth2LoginSuccessHandler;
import com.example.ssauc.user.login.service.CustomOAuth2UserService;
import com.example.ssauc.user.login.service.RefreshTokenService;
import com.example.ssauc.user.login.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtUtil jwtUtil;
    private final CustomOAuth2FailureHandler customOAuth2FailureHandler;
    private final @Lazy CustomOAuth2UserService customOAuth2UserService;
    private final UsersRepository usersRepository;

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtUtil, usersRepository);
    }

    @Bean
    public OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler(RefreshTokenService refreshTokenService) {
        return new OAuth2LoginSuccessHandler(jwtUtil, refreshTokenService);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, RefreshTokenService refreshTokenService) throws Exception {
        http
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
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
                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                        .successHandler(oAuth2LoginSuccessHandler(refreshTokenService))
                        .failureHandler(customOAuth2FailureHandler)
                )
                .formLogin(form -> form.disable())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessHandler(new CustomLogoutSuccessHandler(refreshTokenService, jwtUtil))
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID", "jwt_access", "jwt_refresh")
                )
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
