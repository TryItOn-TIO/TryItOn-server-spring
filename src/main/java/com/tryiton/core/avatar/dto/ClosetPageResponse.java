package com.tryiton.core.avatar.dto;

import com.tryiton.core.closet.dto.ClosetResponse;
import com.tryiton.core.product.dto.ProductResponseDto;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ClosetPageResponse {

    private AvatarProductInfoDto latestAvatar;
    private List<ClosetResponse> closetItems;
    private List<ProductResponseDto> wishlistProducts; // 찜한 상품 리스트
}
