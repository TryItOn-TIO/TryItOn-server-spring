package com.tryiton.core.member.dto;

import lombok.Getter;

@Getter
public class SigninResponseDto {
    private String username;
    private String email;
    private String accessToken;

    public SigninResponseDto(String username, String email, String accessToken) {
        this.username = username;
        this.email = email;
        this.accessToken = accessToken;
    }
}