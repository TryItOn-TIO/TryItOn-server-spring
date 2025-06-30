package com.tryiton.core.payment.dto;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PaymentConfirmRequestDto {
    private String paymentKey;
    private String orderId;
    private Long amount;
}