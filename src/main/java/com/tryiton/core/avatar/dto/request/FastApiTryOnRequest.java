package com.tryiton.core.avatar.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
public class FastApiTryOnRequest {

    private String baseImgUrl;
    private String garmentImgUrl;
    private String maskImgUrl;
    private String poseImgUrl;
    private Long userId;

    public FastApiTryOnRequest(String baseImgUrl, String garmentImgUrl, String maskImgUrl,
        String poseImgUrl, Long userId) {
        this.baseImgUrl = baseImgUrl;
        this.garmentImgUrl = garmentImgUrl;
        this.maskImgUrl = maskImgUrl;
        this.poseImgUrl = poseImgUrl;
        this.userId = userId;
    }
}
