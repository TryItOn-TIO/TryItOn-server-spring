package com.tryiton.core.order.dto;
import lombok.Getter;
import java.util.List;

@Getter
public class OrderRequestDto {
    private Long userId;
    private Long addressId;
    private List<OrderItemRequest> orderItems;
    @Getter public static class OrderItemRequest { private Long variantId; private int quantity; }
}