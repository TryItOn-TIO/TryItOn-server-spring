package com.tryiton.core.product.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "variant_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false, length = 50)
    private String size;

    @Column(nullable = false, length = 50)
    private String color;

    @Column(nullable = false)
    private int quantity;

    // 상품의 실제 가격을 반환하는 편의 메소드
    public BigDecimal getPrice() {
        // 실제로는 할인율(sale)을 계산해야 하지만, 여기서는 정가(price)를 반환
        return BigDecimal.valueOf(this.product.getPrice());
    }
}
