package com.tryiton.core.recommend.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CollaborativeFilteringMatrix {

    private double[][] userItemMatrix;
    private List<Long> userIds;
    private List<Long> productIds;
}
