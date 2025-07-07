package com.tryiton.core.product.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryProductGroup {
    private Long categoryId;
    private String categoryName;
    private List<ProductSummary> products;
}
