package com.tryiton.core.recommend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class LambdaProductDto {

    @JsonProperty("product_id")
    private Long productId;

    @JsonProperty("product_name")
    private String productName;

    private String brand;

    private String img1;

    private Integer price;

    @JsonProperty("tag_match_score")
    private Integer tagMatchScore;

    @JsonProperty("create_at")
    private String createAt;
}
