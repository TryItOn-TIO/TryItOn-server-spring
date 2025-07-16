// src/main/java/com/tryiton/core/avatar/dto/request/FastApiGenerateRequest.java
package com.tryiton.core.avatar.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FastApiGenerateRequest {
    private String tryOnImgUrl;
    private Long userId;
    private String taskId;
    private String callbackUrl;
}