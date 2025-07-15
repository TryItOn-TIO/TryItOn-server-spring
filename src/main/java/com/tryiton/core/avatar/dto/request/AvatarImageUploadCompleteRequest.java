package com.tryiton.core.avatar.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AvatarImageUploadCompleteRequest {
    private String newAvatarImageUrl;  // 프리사인드로 업로드된 새 아바타 이미지 URL
}
