package com.tryiton.core.order.dto;
import com.tryiton.core.order.entity.Order;
import lombok.Getter;
import java.math.BigDecimal;
@Getter
public class OrderResponseDto {
    private final String orderId;
    private final String orderName;
    private final BigDecimal amount;

    public OrderResponseDto(Order order, String orderName) {
        this.orderId = order.getOrderUid();
        this.orderName = orderName;
        this.amount = order.getTotalAmount();
    }
}