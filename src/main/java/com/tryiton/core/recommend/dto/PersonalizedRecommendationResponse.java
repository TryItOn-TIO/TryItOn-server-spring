package com.tryiton.core.recommend.dto;

import com.tryiton.core.product.entity.Product;
import java.util.Collections;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class PersonalizedRecommendationResponse {
    private boolean success;
    private Long userId;
    private List<Product> recommendations = Collections.emptyList();
    private boolean fromCache;
    private String error;
}
