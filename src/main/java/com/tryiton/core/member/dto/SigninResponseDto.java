package com.tryiton.core.member.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SigninResponseDto {
    private String username;
    private String email;
    private String accessToken;
}