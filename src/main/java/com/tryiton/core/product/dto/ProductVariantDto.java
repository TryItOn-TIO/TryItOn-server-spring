package com.tryiton.core.product.dto;

import com.tryiton.core.product.entity.ProductVariant;
import lombok.Getter;

@Getter
public class ProductVariantDto {

    private final Long variantId;
    private final String size;
    private final String color;
    private final Integer quantity;

    public ProductVariantDto(ProductVariant variant) {
        this.variantId = variant.getVariantId();
        this.size = variant.getSize();
        this.color = variant.getColor();
        this.quantity = variant.getQuantity();
    }
}
