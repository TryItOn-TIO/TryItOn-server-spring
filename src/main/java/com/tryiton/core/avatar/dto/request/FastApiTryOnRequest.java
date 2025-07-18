// src/main/java/com/tryiton/core/avatar/dto/request/FastApiTryOnRequest.java (수정)
package com.tryiton.core.avatar.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FastApiTryOnRequest {
    private String baseImgUrl;
    private String garmentImgUrl;
    private String maskImgUrl;
    private String poseImgUrl;
    private Long userId;
    private Long productId;
    private String garmentType;
    private String cacheKey;  // 추가: S3에 저장할 캐시 키
    private String taskId;
    private String callbackUrl;
}
