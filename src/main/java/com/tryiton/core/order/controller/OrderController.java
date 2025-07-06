package com.tryiton.core.order.controller;
import com.tryiton.core.order.dto.*;
import com.tryiton.core.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;
    
    @PostMapping
    public ResponseEntity<OrderResponseDto> createOrder(
        @RequestBody OrderRequestDto requestDto,
        Authentication authentication
    ) {
        // JWT에서 사용자 정보 추출
        String userEmail = authentication.getName();
        
        return ResponseEntity.ok(orderService.createOrder(requestDto, userEmail));
    }
    
    @DeleteMapping("/{orderId}")
    public ResponseEntity<Void> cancelOrder(
        @PathVariable Long orderId,
        Authentication authentication
    ) {
        // JWT에서 사용자 정보 추출
        String userEmail = authentication.getName();
        
        orderService.cancelOrder(orderId, userEmail);
        return ResponseEntity.ok().build();
    }
}
