package com.tryiton.core.payment.service;

import com.tryiton.core.common.exception.BusinessException;
import com.tryiton.core.order.entity.Order;
import com.tryiton.core.order.repository.OrderRepository;
import com.tryiton.core.payment.dto.PaymentConfirmRequestDto;
import com.tryiton.core.payment.entity.Payment;
import com.tryiton.core.payment.repository.PaymentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final RestTemplate restTemplate;
    @Value("${payment.toss.secretKey}")
    private String secretKey;
    private final String TOSS_API_URL = "https://api.tosspayments.com/v1/payments/confirm";

    @Transactional
    public JSONObject confirmPayment(PaymentConfirmRequestDto requestDto) {
        // 필수 파라미터 검증
        if (requestDto.getOrderId() == null || requestDto.getOrderId().trim().isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "주문 ID가 필요합니다.");
        }
        
        if (requestDto.getPaymentKey() == null || requestDto.getPaymentKey().trim().isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "결제 키가 필요합니다.");
        }
        
        if (requestDto.getAmount() == null || requestDto.getAmount() <= 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "유효한 결제 금액이 필요합니다.");
        }
        
        Order order = orderRepository.findByOrderUid(requestDto.getOrderId())
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "주문을 찾을 수 없습니다: " + requestDto.getOrderId()));
        
        if (order.getTotalAmount().longValue() != requestDto.getAmount()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "주문 금액이 일치하지 않습니다.");
        }

        HttpHeaders headers = createTossAuthHeaders();
        JSONObject params = new JSONObject();
        params.put("orderId", requestDto.getOrderId());
        params.put("amount", requestDto.getAmount());
        params.put("paymentKey", requestDto.getPaymentKey());

        HttpEntity<String> requestEntity = new HttpEntity<>(params.toString(), headers);

        try {
            ResponseEntity<JSONObject> responseEntity = restTemplate.postForEntity(TOSS_API_URL, requestEntity, JSONObject.class);
            if (responseEntity.getStatusCode() == HttpStatus.OK) {
                JSONObject paymentData = responseEntity.getBody();
                order.completePayment();
                Payment payment = buildPaymentEntity(order, paymentData);
                paymentRepository.save(payment);
                return paymentData;
            } else {
                throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "토스페이먼츠 승인에 실패했습니다.");
            }
        } catch (Exception e) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "결제 승인 과정에서 오류가 발생했습니다.");
        }
    }

    private HttpHeaders createTossAuthHeaders() {
        String encodedAuth = Base64.getEncoder().encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(encodedAuth);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        return headers;
    }

    private Payment buildPaymentEntity(Order order, JSONObject paymentData) {
        String approvedAtStr = (String) paymentData.get("approvedAt");
        return Payment.builder()
                .order(order)
                .paymentKey((String) paymentData.get("paymentKey"))
                .paymentMethod((String) paymentData.get("method"))
                .amount(BigDecimal.valueOf(order.getTotalAmount().longValue()))
                .status((String) paymentData.get("status"))
                .requestedAt(LocalDateTime.now())
                .approvedAt(LocalDateTime.parse(approvedAtStr, DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                .build();
    }
}
