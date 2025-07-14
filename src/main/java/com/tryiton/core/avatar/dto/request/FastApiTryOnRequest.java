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
    private Long productId;        // 상품 ID 추가
    private String garmentType;    // "top" 또는 "bottom" 추가

    public FastApiTryOnRequest(String baseImgUrl, String garmentImgUrl, String maskImgUrl,
        String poseImgUrl, Long userId, Long productId, String garmentType) {
        this.baseImgUrl = baseImgUrl;
        this.garmentImgUrl = garmentImgUrl;
        this.maskImgUrl = maskImgUrl;
        this.poseImgUrl = poseImgUrl;
        this.userId = userId;
        this.productId = productId;
        this.garmentType = garmentType;
    }
}
