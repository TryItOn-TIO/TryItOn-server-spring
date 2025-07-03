package com.tryiton.core.order.entity;

import com.tryiton.core.product.entity.Product;
import com.tryiton.core.product.entity.ProductVariant;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(name = "order_item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "variant_id", nullable = false)
    private ProductVariant variant;

    @Column(nullable = false) private int quantity;
    @Column(nullable = false, precision = 10, scale = 2) private BigDecimal unitPrice;

    @Builder
    public OrderItem(Order order, Product product, ProductVariant variant, int quantity, BigDecimal unitPrice) {
        this.order = order;
        this.product = product;
        this.variant = variant;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    //== 연관관계 편의 메소드 ==//
    // Order 엔티티에서 호출될 때, 이 OrderItem의 order 필드를 설정합니다.
    // 다른 곳에서 임의로 호출하는 것을 막기 위해 protected 또는 package-private으로 설정하는 것이 좋습니다.
    protected void setOrder(Order order) {
        this.order = order;
    }
}
