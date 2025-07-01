package com.tryiton.core.avatar.dto.response;

public class AvatarTryOnResponse {
    // 가상 피팅이 완료된 최종 이미지의 S3 URL
    private final Long avatarId;
    private final String avatarImgUrl;
    private final String productName;
    private final String categoryName;


    public AvatarTryOnResponse(Long avatarId, String avatarImgUrl, String productName,
        String categoryName) {
        this.avatarId = avatarId;
        this.avatarImgUrl = avatarImgUrl;
        this.productName = productName;
        this.categoryName = categoryName;
    }

}
