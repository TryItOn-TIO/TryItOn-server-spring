package com.tryiton.core.product.dto;

import lombok.Data;

import com.tryiton.core.product.entity.Product;
import lombok.Getter;

@Getter
public class RecommendationResponseDto {
    private final Long productId;
    private final String productName;
    private final String brand;
    private final String imageUrl;
    private Double recommendationScore; // 최종 추천 점수

    public RecommendationResponseDto(Product product, Double score) {
        this.productId = product.getId();
        this.productName = product.getProductName();
        this.brand = product.getBrand();
        this.imageUrl = product.getImg1();
        this.recommendationScore = score;
    }
}