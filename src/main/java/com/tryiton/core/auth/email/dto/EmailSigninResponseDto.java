package com.tryiton.core.auth.email.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EmailSigninResponseDto {
    private String username;
    private String email;
    private String accessToken;
}