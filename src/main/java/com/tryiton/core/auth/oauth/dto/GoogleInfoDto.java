package com.tryiton.core.auth.oauth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GoogleInfoDto {
    private String email;
    private String pictureUrl;
    private String sub; // Google의 고유 사용자 ID
}
