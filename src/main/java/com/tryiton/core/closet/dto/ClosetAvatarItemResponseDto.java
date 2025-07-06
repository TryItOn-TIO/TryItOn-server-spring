package com.tryiton.core.closet.dto;

import com.tryiton.core.product.entity.Product;
import lombok.Getter;

@Getter
public class ClosetAvatarItemResponseDto {

    private final Long productId;
    private final String productName;
    private final String brand;

    public ClosetAvatarItemResponseDto(Product product) {
        this.productId = product.getId();
        this.productName = product.getProductName();
        this.brand = product.getBrand();
    }
}
