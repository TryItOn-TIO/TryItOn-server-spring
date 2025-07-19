package com.tryiton.core.avatar.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AvatarImageUploadCompleteRequest {
    private String newAvatarImageUrl;  // 프리사인드로 업로드된 새 아바타 이미지 URL
    private boolean skipAvatarSetup;   // 아바타 설정하지 않음 옵션 (true: 기본 아바타 사용)
}
