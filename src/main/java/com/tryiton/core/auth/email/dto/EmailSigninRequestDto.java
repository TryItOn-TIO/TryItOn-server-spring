package com.tryiton.core.auth.email.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class EmailSigninRequestDto {
    private String email;
    private String password;
}
