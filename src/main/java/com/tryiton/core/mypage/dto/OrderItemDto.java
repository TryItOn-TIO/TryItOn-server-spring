package com.tryiton.core.mypage.dto;

import com.tryiton.core.order.entity.OrderItem;
import lombok.Getter;

@Getter
public class OrderItemDto {
    private final String productName;
    private final String brand;
    private final String imageUrl;
    private final int quantity;
    private final int price;

    public OrderItemDto(OrderItem orderItem) {
        this.productName = orderItem.getProduct().getProductName();
        this.brand = orderItem.getProduct().getBrand();
        this.imageUrl = orderItem.getProduct().getImg1();
        this.quantity = orderItem.getQuantity();
        this.price = orderItem.getUnitPrice().intValue();
    }
}