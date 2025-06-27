package com.tryiton.core.auth.oauth.controller;

import com.tryiton.core.auth.oauth.service.AuthService;
import com.tryiton.core.auth.oauth.dto.GoogleSigninRequestDto;
import com.tryiton.core.auth.oauth.dto.GoogleSignupRequestDto;
import com.tryiton.core.auth.oauth.dto.GoogleSignupResponseDto;
import com.tryiton.core.member.dto.SigninResponseDto;
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
    public SigninResponseDto login(@RequestBody GoogleSigninRequestDto dto) {
        return authService.loginWithGoogle(dto);
    }

    @PostMapping("/signup")
    public GoogleSignupResponseDto signup(@RequestBody GoogleSignupRequestDto dto) {
        return authService.signupWithGoogle(dto);
    }
}
