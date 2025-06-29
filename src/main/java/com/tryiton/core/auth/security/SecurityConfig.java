package com.tryiton.core.auth.security;

import com.tryiton.core.auth.jwt.JwtUtil;
import com.tryiton.core.auth.jwt.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private  final JwtUtil jwtUtil;
    private final CustomUserDetailService customUserDetailService;

    public SecurityConfig(JwtUtil jwtUtil, CustomUserDetailService customUserDetailService) {
        this.jwtUtil = jwtUtil;
        this.customUserDetailService = customUserDetailService;
    }

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
            .requestMatchers("/**").permitAll() // 로그인 이전 테스트를 위하여 임시로 모든 권한 제한 해제
            /*
            .requestMatchers(
                "/", "/auth/**", "/h2-console/**",
                "/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**", "/webjars/**"
            ).permitAll()
            .requestMatchers("/admin").hasRole("ADMIN")
            .requestMatchers("/product").hasAnyRole("ADMIN", "USER") // api white list 추가
            .anyRequest().authenticated()
             */
        );

        // JWT 인증 필터
        JwtAuthFilter jwtAuthFilter = new JwtAuthFilter(jwtUtil, customUserDetailService);
        http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}