package com.tryiton.core.product.dto;

import com.tryiton.core.avatar.dto.AvatarProductInfoDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Page;

@Getter
@AllArgsConstructor
public class CategoryProductResponse {

    private Page<ProductResponseDto> products;
    private AvatarProductInfoDto avatarInfo;
}
