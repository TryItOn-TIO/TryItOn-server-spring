package com.tryiton.core.avatar.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class AvatarImageUploadCompleteResponse {
    private String message;
    private String newAvatarImageUrl;
    private String newAvatarAssetsUrl;  // 새로 생성된 아바타 에셋 URL (마스크, 포즈 등)
    private boolean success;
    
    public static AvatarImageUploadCompleteResponse success(String newAvatarImageUrl, String newAvatarAssetsUrl) {
        return AvatarImageUploadCompleteResponse.builder()
                .message("아바타 이미지가 성공적으로 업데이트되었습니다.")
                .newAvatarImageUrl(newAvatarImageUrl)
                .newAvatarAssetsUrl(newAvatarAssetsUrl)
                .success(true)
                .build();
    }
    
    public static AvatarImageUploadCompleteResponse failure(String message) {
        return AvatarImageUploadCompleteResponse.builder()
                .message(message)
                .success(false)
                .build();
    }
}
