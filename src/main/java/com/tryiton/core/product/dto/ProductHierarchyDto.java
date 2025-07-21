package com.tryiton.core.product.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class ProductHierarchyDto {
    @JsonProperty("id")
    private Long productId;
    private String productName;
    private String img1;
    private Integer price;
    private Integer sale;
    private String brand;
    private Long wishlistCount;
    private LocalDateTime createAt;
    private Long categoryId;
    private String categoryName;
    private Boolean isLiked;

    public void setIsLiked(boolean isLiked) {
        this.isLiked = isLiked;
    }

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
