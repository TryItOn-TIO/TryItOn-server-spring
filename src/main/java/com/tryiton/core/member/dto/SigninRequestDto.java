package com.tryiton.core.member.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class SigninRequestDto {
    private String email;
    private String password;
}
