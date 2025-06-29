package com.tryiton.core.member.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor
// Google, Email DTO로 확장
public class SigninResponseDto {
    private String username;
    private String email;
    private String accessToken;
}