package com.tryiton.core.product.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SearchProductResponse {

    private List<ProductResponseDto> products;
    private final Long totalCount;
}