package com.tryiton.core.payment.entity;

import com.tryiton.core.order.entity.Order;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(nullable = false, unique = true)
    private String paymentKey;

    private String paymentMethod;

    @Column(nullable = false)
    private BigDecimal amount;

    // status는 토스 응답으로 채워지므로 @Builder.Default가 필요 없습니다.
    @Column(nullable = false)
    private String status;

    // ★★★ [핵심 수정] ★★★
    // requestedAt 필드는 객체 생성 시 기본값이 필요하므로 @Builder.Default를 다시 추가합니다.
    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime requestedAt = LocalDateTime.now();

    private LocalDateTime approvedAt;
}