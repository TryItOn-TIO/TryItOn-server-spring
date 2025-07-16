package com.tryiton.core.avatar.dto.request.worker;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AvatarTryOnRequest {
    private String baseImgUrl;
    private String garmentImgUrl;
    private String maskImgUrl;
    private String poseImgUrl;
    private Long userId;
    private String taskId;
    private String callbackUrl;
}
