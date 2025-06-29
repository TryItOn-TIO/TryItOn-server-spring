package com.tryiton.core.avatar.dto.request;

import com.tryiton.core.avatar.entity.GarmentType;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AvatarTryOnRequest {

    private GarmentType garmentType;
    private Long avatarId;  //todo S3 정책에 따라서 바뀔듯
    private String poseImgUrl;
    private String maskImgUrl;
    private String baseImgUrl;
    private String garmentImgUrl;


}
