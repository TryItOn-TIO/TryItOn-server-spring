package com.tryiton.core.product.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class ProductHierarchyDto {
    @JsonProperty("id")
    private final Long productId;
    private final String productName;
    private final String img1;
    private final Integer price;
    private final Integer sale;
    private final String brand;
    private final Long wishlistCount;
    private final LocalDateTime createAt;
    private final Long categoryId;
    private final String categoryName;
    private final Boolean isLiked;

    public ProductHierarchyDto(
        Number productId, String productName, String img1,
        Integer price, Integer sale, String brand,
        Number wishlistCount, java.sql.Timestamp createAt,
        Number categoryId, String categoryName, Number isLiked
    ) {
        this.productId = productId.longValue();
        this.productName = productName;
        this.img1 = img1;
        this.price = price;
        this.sale = sale;
        this.brand = brand;
        this.wishlistCount = wishlistCount.longValue();
        this.createAt = (createAt != null) ? createAt.toLocalDateTime() : null;
        this.categoryId = categoryId.longValue();
        this.categoryName = categoryName;
        this.isLiked = isLiked.intValue() == 1;
    }
}
