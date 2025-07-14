package com.tryiton.core.avatar.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AvatarBaseImageUpdateRequest {
    private String newBaseImageUrl;  // 새로운 베이스 이미지 URL
}
