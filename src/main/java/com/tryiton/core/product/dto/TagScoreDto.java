package com.tryiton.core.product.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor // Native Query 매핑을 위한 생성자
public class TagScoreDto {
    private Long tagId;
    private Double score;
}