package com.tryiton.core.avatar.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InitialAvatarResponse {
    private String poseImgUrl;
    private String maskImgUrl;
    private String upperMaskImgUrl;
    private String lowerMaskImgUrl;
    private String avatarImg;
}
