package com.tryiton.core.mypage.dto;

import com.tryiton.core.order.entity.Order;
import lombok.Getter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class OrderHistoryDto {
    private final Long orderId;
    private final String orderUid;
    private final String orderStatus;
    private final BigDecimal totalAmount;
    private final LocalDateTime createdAt;
    private final List<OrderItemDto> orderItems;

    public OrderHistoryDto(Order order) {
        this.orderId = order.getId();
        this.orderUid = order.getOrderUid();
        this.orderStatus = order.getOrderStatus();
        this.totalAmount = order.getTotalAmount();
        this.createdAt = order.getCreatedAt();
        this.orderItems = order.getOrderItems().stream()
                .map(OrderItemDto::new)
                .collect(Collectors.toList());
    }
}
