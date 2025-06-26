package com.tryiton.core.product.dto;

import com.tryiton.core.product.entity.Product;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class ProductResponseDto {

    private final Long id;
    private final String productName;
    private final String img1;
    private final int price;
    private final int sale;
    private final String brand;
    private final int wishlistCount;
    private final LocalDateTime createdAt;

    public ProductResponseDto(Product product) {
        this.id = product.getId();
        this.productName = product.getProductName();
        this.img1 = product.getImg1();
        this.price = product.getPrice();
        this.sale = product.getSale();
        this.brand = product.getBrand();
        this.wishlistCount = product.getWishlistCount();
        this.createdAt = product.getCreatedAt();
    }
}
