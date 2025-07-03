package com.tryiton.core.auth.email.controller;

import com.tryiton.core.auth.email.dto.EmailRequestDto;
import com.tryiton.core.auth.email.dto.EmailSigninRequestDto;
import com.tryiton.core.auth.email.dto.EmailSigninResponseDto;
import com.tryiton.core.auth.email.dto.EmailSignupRequestDto;
import com.tryiton.core.auth.email.dto.EmailSignupResponseDto;
import com.tryiton.core.auth.email.dto.EmailVerifyRequestDto;
import com.tryiton.core.auth.email.service.EmailAuthService;
import jakarta.mail.MessagingException;
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
    public EmailSigninResponseDto login(@RequestBody EmailSigninRequestDto dto){
        return emailAuthService.signinWithEmail(dto);
    }

    @PostMapping("/send")
    public void sendAuthenticationCode(@RequestBody EmailRequestDto dto) throws MessagingException {
        emailAuthService.sendAuthenticationCode(dto);
    }

    @PostMapping("/verify")
    public boolean verifyAuthenticationCode(@RequestBody EmailVerifyRequestDto dto){
        return emailAuthService.verifyAuthenticationCode(dto);
    }

    @PostMapping("/signup")
    public EmailSignupResponseDto signupWithEmail(@RequestBody EmailSignupRequestDto dto){
        return emailAuthService.signupWithEmail(dto);
    }
}
