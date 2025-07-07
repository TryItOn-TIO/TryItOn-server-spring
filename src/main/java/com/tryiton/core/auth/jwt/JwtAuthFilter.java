package com.tryiton.core.auth.jwt;

import com.tryiton.core.auth.security.CustomUserDetails;
import com.tryiton.core.auth.security.CustomUserDetailService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailService customUserDetailService;
    private final AuthenticationEntryPoint authenticationEntryPoint;

    public JwtAuthFilter(JwtUtil jwtUtil, CustomUserDetailService customUserDetailService, 
                        AuthenticationEntryPoint authenticationEntryPoint) {
        this.jwtUtil = jwtUtil;
        this.customUserDetailService = customUserDetailService;
        this.authenticationEntryPoint = authenticationEntryPoint;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String bearer = request.getHeader("Authorization");

        if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
            String token = bearer.substring(7);

            try {
                if(!jwtUtil.isExpired(token)){
                    String email = jwtUtil.getUsername(token);

                    if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                        CustomUserDetails userDetails = (CustomUserDetails) customUserDetailService.loadUserByUsername(email);

                        UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                }
            } catch (ExpiredJwtException e) {
                // JWT 만료 시 AuthenticationEntryPoint로 처리
                AuthenticationException authException = new AuthenticationException("JWT token has expired", e) {};
                authenticationEntryPoint.commence(request, response, authException);
                return;
                
            } catch (JwtException e) {
                // 기타 JWT 관련 예외 처리
                AuthenticationException authException = new AuthenticationException("Invalid JWT token", e) {};
                authenticationEntryPoint.commence(request, response, authException);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
