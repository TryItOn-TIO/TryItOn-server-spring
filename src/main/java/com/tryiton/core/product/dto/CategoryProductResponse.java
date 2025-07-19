package com.tryiton.core.product.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Page;

@Getter
@AllArgsConstructor
public class CategoryProductResponse {

    private Page<ProductSummaryDto> products;
    // private AvatarProductInfoDto avatarInfo;
}
