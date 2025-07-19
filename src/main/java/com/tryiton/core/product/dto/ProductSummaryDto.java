package com.tryiton.core.product.dto;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
public class ProductSummaryDto {

    private Long id;
    private String productName;
    private String img1;
    private int price;
    private int sale;
    private int salePrice;
    private String brand;
    private int wishlistCount;
    private LocalDateTime createdAt;
    private Long categoryId;
    private String categoryName;

    @Setter
    private boolean liked;

    // JPQL 프로젝션을 위한 생성자
    public ProductSummaryDto(Long id, String productName, String img1, int price, int sale, String brand, int wishlistCount, LocalDateTime createdAt, Long categoryId, String categoryName) {
        this.id = id;
        this.productName = productName;
        this.img1 = img1;
        this.price = price;
        this.sale = sale;
        this.brand = brand;
        this.wishlistCount = wishlistCount;
        this.createdAt = createdAt;
        this.categoryId = categoryId;
        this.categoryName = categoryName;

        if (sale > 0) {
            this.salePrice = (int) Math.round(price * (100.0 - sale) / 100.0);
        } else {
            this.salePrice = price;
        }
    }
}
