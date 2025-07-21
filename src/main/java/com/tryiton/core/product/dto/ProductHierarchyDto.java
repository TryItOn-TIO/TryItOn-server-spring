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
        Long productId, String productName, String img1,
        Integer price, Integer sale, String brand,
        Integer wishlistCount, // Long -> Integer로 변경
        LocalDateTime createAt,
        Long categoryId, String categoryName, Integer isLiked
    ) {
        this.productId = productId;
        this.productName = productName;
        this.img1 = img1;
        this.price = price;
        this.sale = sale;
        this.brand = brand;
        this.wishlistCount = wishlistCount != null ? wishlistCount.longValue() : 0L; // 내부에서 Long으로 변환
        this.createAt = createAt;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.isLiked = isLiked != null && isLiked == 1;
    }
}
