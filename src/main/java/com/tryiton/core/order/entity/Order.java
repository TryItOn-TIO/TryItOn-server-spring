package com.tryiton.core.order.entity;

import com.tryiton.core.address.entity.Address;
import com.tryiton.core.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(name = "order_id")
    private Long id;

    @Column(nullable = false, unique = true)
    private String orderUid;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id", nullable = false)
    private Member user;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "addr_id", nullable = false)
    private Address address;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> orderItems = new ArrayList<>();

    @Column(nullable = false) private String orderStatus;
    @Column(nullable = false) private BigDecimal totalAmount;
    @Column(nullable = false) private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt;

    @Builder
    public Order(Member user, Address address, String orderStatus, BigDecimal totalAmount) {
        this.user = user;
        this.address = address;
        this.orderStatus = orderStatus;
        this.totalAmount = totalAmount;
        this.orderUid = UUID.randomUUID().toString();
    }

    public void addOrderItem(OrderItem orderItem) {
        orderItems.add(orderItem);
        orderItem.setOrder(this);
    }

    public void completePayment() {
        this.orderStatus = "COMPLETED";
        this.updatedAt = LocalDateTime.now();
    }
}