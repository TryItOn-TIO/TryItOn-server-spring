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
    private int originalPrice; // 정가
    private int salePercentage; // 할인율
    private int price; // 할인된 가격
    private int quantity;
    private String imageUrl;

    public CartItemDto(CartItem cartItem) {
        this.cartItemId = cartItem.getId();
        this.variantId = cartItem.getVariant().getVariantId();
        this.productName = cartItem.getVariant().getProduct().getProductName();
        this.brand = cartItem.getVariant().getProduct().getBrand();
        this.size = cartItem.getVariant().getSize();
        this.color = cartItem.getVariant().getColor();
        this.originalPrice = cartItem.getVariant().getProduct().getPrice();
        this.salePercentage = cartItem.getVariant().getProduct().getSale();
        this.quantity = cartItem.getQuantity();
        this.imageUrl = cartItem.getVariant().getProduct().getImg1();
        
        // 할인된 가격 계산 (ProductVariant.getPrice()와 동일한 로직)
        if (this.salePercentage > 0) {
            this.price = (int) Math.round(this.originalPrice * (100.0 - this.salePercentage) / 100.0);
        } else {
            this.price = this.originalPrice;
        }
    }
}