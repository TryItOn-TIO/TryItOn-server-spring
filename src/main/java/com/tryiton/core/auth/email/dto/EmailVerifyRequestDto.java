package com.tryiton.core.auth.email.dto;

import lombok.Getter;

@Getter
public class EmailVerifyRequestDto {

    private String email;
    private String code;
}
