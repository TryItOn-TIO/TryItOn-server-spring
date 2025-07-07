package com.tryiton.core.product.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductSummary {
    private Long id;
    private String name;
    private String brand;
    private Integer price;        // 정가
    private Integer sale;         // 할인율 (%)
    private Integer salePrice;    // 할인된 가격
    private String imageUrl;
    private String category;
    private Integer wishlistCount;
}
