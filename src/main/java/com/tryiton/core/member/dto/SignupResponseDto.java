package com.tryiton.core.member.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter @Setter
@SuperBuilder
@NoArgsConstructor
// Google, Email DTO로 확장
public class SignupResponseDto {
    private String email;
    private String username;
}
