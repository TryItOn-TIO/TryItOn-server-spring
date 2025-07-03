package com.tryiton.core.auth.oauth.controller;

import com.tryiton.core.auth.oauth.dto.GoogleSigninResponseDto;
import com.tryiton.core.auth.oauth.service.AuthService;
import com.tryiton.core.auth.oauth.dto.GoogleSigninRequestDto;
import com.tryiton.core.auth.oauth.dto.GoogleSignupRequestDto;
import com.tryiton.core.auth.oauth.dto.GoogleSignupResponseDto;
import com.tryiton.core.common.exception.BusinessException;
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

    // globalExceptionHandler가 등록된 에러를 처리함
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody GoogleSigninRequestDto dto) {
        GoogleSigninResponseDto response = authService.loginWithGoogle(dto);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody GoogleSignupRequestDto dto) {
        GoogleSignupResponseDto response = authService.signupWithGoogle(dto);
        return ResponseEntity.ok(response);
    }
}
