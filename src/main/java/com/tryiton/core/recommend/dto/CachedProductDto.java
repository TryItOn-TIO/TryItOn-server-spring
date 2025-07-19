package com.tryiton.core.recommend.dto;

import com.tryiton.core.product.entity.Product;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class CachedProductDto {
    private Long productId;
    private String productName;
    private String imageUrl;
    private int price;
    private int sale;
    private int salePrice;
    private String brand;
    private long wishlistCount;
    private LocalDateTime createdAt;
    private Long categoryId;
    private String categoryName;

    public CachedProductDto(Product product) {
        this.productId = product.getId();
        this.productName = product.getProductName();
        this.imageUrl = product.getImg1();
        this.price = product.getPrice();
        this.sale = product.getSale();
        if (product.getSale() > 0) {
            this.salePrice = (int) Math.round(product.getPrice() * (100.0 - product.getSale()) / 100.0);
        } else {
            this.salePrice = product.getPrice();
        }
        this.brand = product.getBrand();
        this.wishlistCount = product.getWishlistCount();
        this.createdAt = product.getCreateAt();
        if (product.getCategory() != null) {
            this.categoryId = product.getCategory().getId();
            this.categoryName = product.getCategory().getCategoryName();
        }
    }
}
