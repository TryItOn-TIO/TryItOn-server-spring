package com.tryiton.core.member.dto;

import lombok.Getter;

@Getter
public class SignupRequestDto {
    private String email;
    private String password;
    private String nickname;
    private String preferredStyle;
    private Integer height;  // cm
    private Integer weight;  // kg
    private Integer age;
}
