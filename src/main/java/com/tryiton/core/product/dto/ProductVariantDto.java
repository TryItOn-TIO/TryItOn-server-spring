package com.tryiton.core.product.dto;

import com.tryiton.core.product.entity.ProductVariant;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ProductVariantDto {

    private Long variantId;
    private String size;
    private String color;
    private Integer quantity;

    public ProductVariantDto(ProductVariant variant) {
        this.variantId = variant.getVariantId();
        this.size = variant.getSize();
        this.color = variant.getColor();
        this.quantity = variant.getQuantity();
    }
}
