package com.tryiton.core.avatar.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class InitialAvatarResponse {
    private String tryOnImgUrl;
    private String poseImgUrl;
    private String lowerMaskImgUrl;
    private String upperMaskImgUrl;
}
