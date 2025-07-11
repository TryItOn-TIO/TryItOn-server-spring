package com.tryiton.core.elasticsearch;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ProductIndex {

    private Long id;
    private String productName;
    private String brand;

    @Builder
    public ProductIndex(Long id, String productName, String brand) {
        this.id = id;
        this.productName = productName;
        this.brand = brand;
    }
}
