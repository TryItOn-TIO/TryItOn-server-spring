package com.tryiton.core.payment.controller;
import com.tryiton.core.auth.security.CustomUserDetails;
import com.tryiton.core.member.entity.Member;
import com.tryiton.core.payment.dto.PaymentConfirmRequestDto;
import com.tryiton.core.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.json.simple.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;
    
    @PostMapping("/confirm")
    public ResponseEntity<JSONObject> confirmPayment(
            @RequestBody PaymentConfirmRequestDto requestDto,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Member member = userDetails.getUser();
        return ResponseEntity.ok(paymentService.confirmPayment(requestDto, member));
    }
}