package com.tryiton.core.auth.oauth.controller;

import com.tryiton.core.auth.oauth.service.AuthService;
import com.tryiton.core.common.exception.BusinessException;
import com.tryiton.core.auth.oauth.dto.GoogleSigninRequestDto;
import com.tryiton.core.auth.oauth.dto.GoogleSignupRequestDto;
import com.tryiton.core.auth.oauth.dto.GoogleSignupResponseDto;
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
                .status(HttpStatus.NOT_FOUND) // 404 Not Found
                .body(Map.of("error", e.getMessage(), "needsSignup", true));
        }
    }

    @PostMapping("/signup")
    public GoogleSignupResponseDto signup(@RequestBody GoogleSignupRequestDto dto) {
        return authService.signupWithGoogle(dto);
    }
}
