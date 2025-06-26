package com.tryiton.core.auth.oauth.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class GoogleSigninRequestDto {
    private String idToken; // 프론트에서 넘겨준 Google ID Token
}
