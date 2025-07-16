package com.tryiton.core.avatar.dto.request.worker;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AvatarCreateRequest {
    private String tryOnImgUrl;
    private Long userId;
    private String taskId;
    private String callbackUrl;
}
