package com.tryiton.core.product.dto;

import lombok.Getter;
import org.springframework.data.domain.Page;

@Getter
public class CategoryProductResponse {
    private CategoryResponseDto category;
    private Page<ProductHierarchyDto> products;

    public CategoryProductResponse(CategoryResponseDto category, Page<ProductHierarchyDto> products) {
        this.category = category;
        this.products = products;
    }
}
