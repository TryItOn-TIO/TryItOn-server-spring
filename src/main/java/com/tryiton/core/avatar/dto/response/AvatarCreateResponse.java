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

    private String tryOnImgUrl;

    @Builder
    public AvatarCreateResponse(String tryOnImgUrl) {
        this.tryOnImgUrl = tryOnImgUrl;
    }

    public static AvatarCreateResponse fromEntity(Avatar savedAvatar) {
        return AvatarCreateResponse.builder()
            .tryOnImgUrl(savedAvatar.getAvatarImg())
            .build();
    }
}
