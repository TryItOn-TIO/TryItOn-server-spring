package com.tryiton.core.auth.oauth.controller;

import com.tryiton.core.auth.oauth.service.AuthService;
import com.tryiton.core.auth.oauth.dto.GoogleSigninRequestDto;
import com.tryiton.core.auth.oauth.dto.GoogleSignupRequestDto;
import com.tryiton.core.auth.oauth.dto.GoogleSignupResponseDto;
import com.tryiton.core.common.exception.BusinessException;
import com.tryiton.core.member.dto.SigninResponseDto;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/google")
public class GoogleAuthController {
    private final AuthService authService;

    public GoogleAuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody GoogleSigninRequestDto dto) {
        try {
            SigninResponseDto response = authService.loginWithGoogle(dto);
            return ResponseEntity.ok(response);
        } catch (BusinessException e) {
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(Map.of("code", "LOGIN_FAILED", "message", e.getMessage()));
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody GoogleSignupRequestDto dto) {
        try {
            GoogleSignupResponseDto response = authService.signupWithGoogle(dto);
            return ResponseEntity.ok(response);
        } catch (BusinessException e) {
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("code", "SIGNUP_FAILED", "message", e.getMessage()));
        }
    }
}
