package com.tryiton.core.auth.email.controller;

import com.tryiton.core.auth.email.dto.EmailRequestDto;
import com.tryiton.core.auth.email.dto.EmailSigninRequestDto;
import com.tryiton.core.auth.email.dto.EmailSigninResponseDto;
import com.tryiton.core.auth.email.dto.EmailSignupRequestDto;
import com.tryiton.core.auth.email.dto.EmailSignupResponseDto;
import com.tryiton.core.auth.email.dto.EmailVerifyRequestDto;
import com.tryiton.core.auth.email.service.EmailAuthService;
import jakarta.mail.MessagingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/mail")
public class EmailAuthController {

    private final EmailAuthService emailAuthService;

    public EmailAuthController(EmailAuthService emailAuthService) {
        this.emailAuthService = emailAuthService;
    }

    @PostMapping("/login")
    public ResponseEntity<EmailSigninResponseDto> login(@RequestBody EmailSigninRequestDto dto) {
        EmailSigninResponseDto responseDto = emailAuthService.signinWithEmail(dto);
        return ResponseEntity.ok(responseDto);
    }

//    @PostMapping("/send")
//    public void sendAuthenticationCode(@RequestBody EmailRequestDto dto) throws MessagingException {
//        emailAuthService.sendAuthenticationCode(dto);
//    }

    @PostMapping("/send")
    public ResponseEntity<Void> sendAuthenticationCode(@RequestBody EmailRequestDto dto)
        throws MessagingException {
        emailAuthService.sendAuthenticationCode(dto);
        // 성공적으로 실행되면 200 OK 상태 코드만 반환
        return ResponseEntity.ok().build();
    }

    @PostMapping("/verify")
    public ResponseEntity<Boolean> verifyAuthenticationCode(
        @RequestBody EmailVerifyRequestDto dto) {
        Boolean isVerified = emailAuthService.verifyAuthenticationCode(dto);
        return ResponseEntity.ok(isVerified);
    }

    @PostMapping("/signup")
    public ResponseEntity<EmailSignupResponseDto> signupWithEmail(
        @RequestBody EmailSignupRequestDto dto) {
        EmailSignupResponseDto responseDto = emailAuthService.signupWithEmail(dto);
        // 리소스가 성공적으로 생성되었음을 의미하는 201 Created 상태 코드와 함께 결과 반환
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }
}
