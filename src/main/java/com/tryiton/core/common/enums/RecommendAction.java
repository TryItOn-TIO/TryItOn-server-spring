package com.tryiton.core.common.enums;

public enum RecommendAction {
    CLICK(0.5f),
    WISHLIST(1.0f),
    CART(2.0f),
    BUY(3.0f),
    TRYON(2.0f);

    private final float score;

    RecommendAction(float score) {
        this.score = score;
    }

    public float getScore() {
        return score;
    }
}
