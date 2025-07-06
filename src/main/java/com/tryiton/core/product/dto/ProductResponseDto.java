package com.tryiton.core.product.dto;

import com.tryiton.core.product.entity.Product;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class ProductResponseDto {

    private final Long id;
    private final String productName;
    private final String img1;
    private final int price; // 정가
    private final int sale; // 할인율 (%)
    private final int salePrice; // 할인된 가격
    private final boolean liked; // 유저가 찜한 상품인지 여부
    private final String brand;
    private final int wishlistCount;
    private final LocalDateTime createdAt;
    private final Long categoryId;
    private final String categoryName;

    public ProductResponseDto(Product product, boolean liked) {
        this.id = product.getId();
        this.productName = product.getProductName();
        this.img1 = product.getImg1();
        this.price = product.getPrice();
        this.sale = product.getSale();
        this.liked = liked;
        this.brand = product.getBrand();
        this.wishlistCount = product.getWishlistCount();
        this.createdAt = product.getCreatedAt();
        this.categoryId = product.getCategory().getId();
        this.categoryName = product.getCategory().getCategoryName();
        
        // 할인된 가격 계산
        if (product.getSale() > 0) {
            this.salePrice = (int) Math.round(product.getPrice() * (100.0 - product.getSale()) / 100.0);
        } else {
            this.salePrice = product.getPrice(); // 할인이 없으면 정가와 동일
        }
    }
}
