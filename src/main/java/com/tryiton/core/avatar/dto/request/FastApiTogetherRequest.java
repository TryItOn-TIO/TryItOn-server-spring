package com.tryiton.core.avatar.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FastApiTogetherRequest {
    private String avatarImgUrl;
    private String topImgUrl;
    private String bottomImgUrl;
    private String upperMaskImgUrl;
    private String lowerMaskImgUrl;
    private String poseImgUrl;
    private Long userId;
}