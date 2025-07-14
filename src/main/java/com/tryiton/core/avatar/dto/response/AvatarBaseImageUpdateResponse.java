package com.tryiton.core.avatar.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class AvatarBaseImageUpdateResponse {
    private String message;
    private String newBaseImageUrl;
    private String newAvatarImageUrl;  // 새로 생성된 아바타 이미지 URL
    private boolean success;
    
    public static AvatarBaseImageUpdateResponse success(String newBaseImageUrl, String newAvatarImageUrl) {
        return AvatarBaseImageUpdateResponse.builder()
                .message("아바타 베이스 이미지가 성공적으로 업데이트되었습니다.")
                .newBaseImageUrl(newBaseImageUrl)
                .newAvatarImageUrl(newAvatarImageUrl)
                .success(true)
                .build();
    }
    
    public static AvatarBaseImageUpdateResponse failure(String message) {
        return AvatarBaseImageUpdateResponse.builder()
                .message(message)
                .success(false)
                .build();
    }
}
