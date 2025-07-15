package com.tryiton.core.recommend.dto;

import com.tryiton.core.common.enums.RecommendAction;
import lombok.Getter;

@Getter
public class BehaviorRequestDto {

    private Long userId;
    private Long productId;
    private RecommendAction action;
    private float score;
}
