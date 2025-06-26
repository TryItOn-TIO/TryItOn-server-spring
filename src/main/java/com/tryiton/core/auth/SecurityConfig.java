package com.tryiton.core.auth;

import com.tryiton.core.auth.oauth.handler.CustomOAuth2SuccessHandler;
import com.tryiton.core.auth.oauth.service.CustomOAuth2UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomOAuth2UserService oAuth2UserService;

    public SecurityConfig(CustomOAuth2UserService oAuth2UserService,
        CustomOAuth2SuccessHandler successHandler) {
        this.oAuth2UserService = oAuth2UserService;
        this.successHandler = successHandler;
    }

    private final CustomOAuth2SuccessHandler successHandler;


    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        // CORS 설정
        http.cors(cors -> cors.configurationSource(request -> {
            CorsConfiguration config = new CorsConfiguration();
            config.setAllowedOrigins(List.of("http://localhost:3000"));
            config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
            config.setAllowedHeaders(List.of("*"));
            config.setExposedHeaders(List.of("Authorization"));
            config.setAllowCredentials(true);
            return config;
        }));

        // CSRF, 세션, 기본 로그인 비활성화
        http.csrf(csrf -> csrf.disable());
        http.headers(headers -> headers.frameOptions(frame -> frame.disable()));
        http.formLogin(login -> login.disable());
        http.httpBasic(basic -> basic.disable());
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // 인가 정책
        http.authorizeHttpRequests(auth -> auth
            .requestMatchers(
                "/", "/api/auth/**", "/h2-console/**",
                "/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**", "/webjars/**"
            ).permitAll()
            .requestMatchers("/admin/**").hasRole("ADMIN")
            .requestMatchers("/api/mypage/**").hasAnyRole("ADMIN", "USER") // api white list 추가
            .anyRequest().authenticated()
        );

        // OAuth 로그인
        http.oauth2Login(oauth -> oauth
            .loginPage("/login")
            .userInfoEndpoint(user -> user.userService(oAuth2UserService))
            .successHandler(successHandler)
        );

        return http.build();
    }
}