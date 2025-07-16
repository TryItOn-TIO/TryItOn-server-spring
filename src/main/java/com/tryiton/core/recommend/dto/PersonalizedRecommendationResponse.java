package com.tryiton.core.recommend.dto;

import java.util.Collections;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class PersonalizedRecommendationResponse {
    private boolean success;
    private Long userId;
    private List<LambdaProductDto> recommendations = Collections.emptyList();
    private boolean fromCache;
    private String error;
}
