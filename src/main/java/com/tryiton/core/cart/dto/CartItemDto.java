package com.tryiton.core.cart.dto;

import com.tryiton.core.cart.entity.CartItem;
import lombok.Getter;

@Getter
public class CartItemDto {
    private Long cartItemId;
    private Long variantId;
    private String productName;
    private String brand;
    private String size;
    private String color;
    private int price;
    private int quantity;
    private String imageUrl;

    public CartItemDto(CartItem cartItem) {
        this.cartItemId = cartItem.getId();
        this.variantId = cartItem.getVariant().getVariantId();
        this.productName = cartItem.getVariant().getProduct().getProductName();
        this.brand = cartItem.getVariant().getProduct().getBrand();
        this.size = cartItem.getVariant().getSize();
        this.color = cartItem.getVariant().getColor();
        this.price = cartItem.getVariant().getProduct().getPrice();
        this.quantity = cartItem.getQuantity();
        this.imageUrl = cartItem.getVariant().getProduct().getImg1();
    }
}