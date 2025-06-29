package com.tryiton.core.avatar.dto.response;

import com.tryiton.core.avatar.entity.Avatar;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AvatarCreateResponse {

    private String avatarImgUrl;

    @Builder
    public AvatarCreateResponse(String avatarImgUrl) {
        this.avatarImgUrl = avatarImgUrl;
    }

    public static AvatarCreateResponse fromEntity(Avatar savedAvatar) {
        return AvatarCreateResponse.builder()
            .avatarImgUrl(savedAvatar.getAvatarImg())
            .build();
    }
}
