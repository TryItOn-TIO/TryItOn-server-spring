package com.tryiton.core.payment.controller;
import com.tryiton.core.payment.dto.PaymentConfirmRequestDto;
import com.tryiton.core.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.json.simple.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;
    
    @PostMapping("/confirm")
    public ResponseEntity<JSONObject> confirmPayment(@RequestBody PaymentConfirmRequestDto requestDto) {
        return ResponseEntity.ok(paymentService.confirmPayment(requestDto));
    }
}