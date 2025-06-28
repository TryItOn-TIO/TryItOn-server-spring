package com.tryiton.core.product.dto;

import lombok.Getter;
import java.util.List;

@Getter
public class MainProductResponse {

    private final List<ProductResponseDto> recommended;
    private final List<ProductResponseDto> ranked;
    private final String tryOnImg;

    public MainProductResponse(List<ProductResponseDto> recommended,
        List<ProductResponseDto> ranked, String tryOnImg) {
        this.recommended = recommended;
        this.ranked = ranked;
        this.tryOnImg = tryOnImg;
    }
}
