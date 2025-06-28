package com.tryiton.core.product.dto;

import lombok.Getter;
import org.springframework.data.domain.Page;
import java.util.List;

@Getter
public class MainProductResponse {

    private final List<ProductResponseDto> recommended;
    private final Page<ProductResponseDto> ranked;

    public MainProductResponse(List<ProductResponseDto> recommended,
        Page<ProductResponseDto> ranked) {
        this.recommended = recommended;
        this.ranked = ranked;
    }
}
