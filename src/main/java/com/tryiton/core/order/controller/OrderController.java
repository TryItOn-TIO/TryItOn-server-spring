package com.tryiton.core.order.controller;
import com.tryiton.core.order.dto.*;
import com.tryiton.core.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;
    @PostMapping
    public ResponseEntity<OrderResponseDto> createOrder(@RequestBody OrderRequestDto requestDto) {
        return ResponseEntity.ok(orderService.createOrder(requestDto));
    }
}
