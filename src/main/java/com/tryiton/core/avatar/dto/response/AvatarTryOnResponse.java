package com.tryiton.core.avatar.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AvatarTryOnResponse {
    // 가상 피팅이 완료된 최종 이미지의 S3 URL
    private Long avatarId;
    private String avatarImgUrl;
    private List<ProductInfo> products;

    @Getter
    @AllArgsConstructor
    public static class ProductInfo {
        private String productName;
        private String categoryName;
        // 추후 브랜드, 상품 ID 등 필요한 정보를 여기에 쉽게 추가할 수 있습니다.
        // private String brand;
        // private Long productId;
    }


    @Builder
    public AvatarTryOnResponse(Long avatarId, String avatarImgUrl, List<ProductInfo> products) {
        this.avatarId = avatarId;
        this.avatarImgUrl = avatarImgUrl;
        this.products = products;
    }
}
