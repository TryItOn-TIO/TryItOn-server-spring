package com.tryiton.core.product.dto;

import com.tryiton.core.avatar.dto.AvatarProductInfoDto;
import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
public class MainProductResponse {

    private final List<ProductResponseDto> recommended;
    private final List<ProductResponseDto> ranked;
    private AvatarProductInfoDto avatarInfo;
}
