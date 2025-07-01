package com.tryiton.core.cart.dto;
import lombok.Getter;
@Getter
public class CartAddRequestDto {
    private Long variantId;
    private int quantity;
}
