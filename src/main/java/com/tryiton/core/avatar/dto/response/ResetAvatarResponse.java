package com.tryiton.core.avatar.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResetAvatarResponse {
    
    @Builder.Default
    private boolean success = true;
    
    @Builder.Default
    private String message = "아바타가 성공적으로 초기화되었습니다.";
    
    private String avatarImageUrl;
    
    public static ResetAvatarResponse of(String avatarImageUrl) {
        return ResetAvatarResponse.builder()
                .success(true)
                .message("아바타가 성공적으로 초기화되었습니다.")
                .avatarImageUrl(avatarImageUrl)
                .build();
    }
}
