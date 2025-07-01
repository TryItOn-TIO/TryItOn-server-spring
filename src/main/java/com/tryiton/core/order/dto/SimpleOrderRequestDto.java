package com.tryiton.core.order.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class SimpleOrderRequestDto {
    private String orderName;
    private BigDecimal amount;
    private Long userId;
    private Long addressId;
}
