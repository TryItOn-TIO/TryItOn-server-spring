package com.tryiton.core.avatar.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class InitialAvatarRequest {
    private Long userId;
    private String tryOnImgUrl;
}
