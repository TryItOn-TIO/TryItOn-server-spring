package com.tryiton.core.auth.oauth.dto;

import lombok.Getter;

@Getter
public class GoogleInfoDto {
    private String email;
    private String pictureUrl;

    public GoogleInfoDto(String email, String pictureUrl) {
        this.email = email;
        this.pictureUrl = pictureUrl;
    }
}
