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
    private String poseImgUrl;
    private String lowerMaskImgUrl;
    private String upperMaskImgUrl;



    @Builder
    public AvatarCreateResponse(String tryOnImgUrl, String poseImgUrl, String lowerMaskImgUrl, String upperMaskImgUrl) {
        this.tryOnImgUrl = tryOnImgUrl;
        this.poseImgUrl = poseImgUrl;
        this.lowerMaskImgUrl = lowerMaskImgUrl;
        this.upperMaskImgUrl = upperMaskImgUrl;
    }

    public static AvatarCreateResponse fromEntity(Avatar savedAvatar) {
        return AvatarCreateResponse.builder()
            .tryOnImgUrl(savedAvatar.getAvatarImg())
            .poseImgUrl(savedAvatar.getPoseImg())
            .upperMaskImgUrl(savedAvatar.getUpperMaskImg())
            .lowerMaskImgUrl(savedAvatar.getLowerMaskImg())
            .build();
    }
}
