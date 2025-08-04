package com.tryiton.core.order.dto;
import lombok.Getter;
import java.math.BigDecimal;
import java.util.List;

@Getter
public class OrderRequestDto {
    private Long addressId;
    private BigDecimal amount; // 프론트엔드에서 계산한 금액 (검증용, 선택적)
    private List<OrderItemRequest> orderItems;
    
    @Getter 
    public static class OrderItemRequest { 
        private Long variantId; 
        private int quantity; 
    }
}