package com.tryiton.core.avatar.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InitialAvatarResponse {
    private String tryOnImgUrl;
    private String poseImgUrl;
    private String lowerMaskImgUrl;
    private String upperMaskImgUrl;
}
