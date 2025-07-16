package com.tryiton.core.avatar.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FastApiCombinationRequest {
    private Long userId;
    private String baseImgUrl;
    private Long topProductId;      // 상의 상품 ID
    private String topImgUrl;       // 상의 이미지 URL
    private Long bottomProductId;   // 하의 상품 ID  
    private String bottomImgUrl;    // 하의 이미지 URL
    private String maskUrl;
    private String poseUrl;
}
