package com.tryiton.core.order.dto;
import lombok.Getter;
import java.math.BigDecimal;
import java.util.List;

@Getter
public class OrderRequestDto {
    private Long addressId;
    private BigDecimal amount; // 금액 검증을 위해 추가
    private List<OrderItemRequest> orderItems;
    
    @Getter 
    public static class OrderItemRequest { 
        private Long variantId; 
        private int quantity; 
    }
}