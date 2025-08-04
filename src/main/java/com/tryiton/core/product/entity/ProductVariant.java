package com.tryiton.core.product.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
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
    private Long variantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false, length = 50)
    private String size;

    @Column(nullable = false, length = 50)
    private String color;

    @Column(nullable = false)
    private int quantity;
    
    @Version
    private Long version; // 낙관적 락을 위한 버전 필드

    // 상품의 실제 가격을 반환하는 편의 메소드 (할인율 적용)
    public BigDecimal getPrice() {
        int originalPrice = this.product.getPrice();
        int salePercentage = this.product.getSale();
        
        // 할인율이 0이면 정가 반환
        if (salePercentage == 0) {
            return BigDecimal.valueOf(originalPrice);
        }
        
        // 할인율 적용: 정가 * (100 - 할인율) / 100
        double discountedPrice = originalPrice * (100.0 - salePercentage) / 100.0;
        return BigDecimal.valueOf(Math.round(discountedPrice));
    }
    
    // 재고 확인 메서드
    public int getStock() {
        return this.quantity;
    }
    
    // 재고 차감 메서드
    public void decreaseStock(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("차감할 수량은 0보다 커야 합니다.");
        }
        if (this.quantity < amount) {
            throw new IllegalArgumentException("재고가 부족합니다. 현재 재고: " + this.quantity + ", 요청 수량: " + amount);
        }
        this.quantity -= amount;
    }
    
    // 재고 증가 메서드 (주문 취소 시 사용)
    public void increaseStock(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("증가할 수량은 0보다 커야 합니다.");
        }
        this.quantity += amount;
    }
    
    @Builder
    public ProductVariant(Long variantId, Product product, String size, String color,
                          Integer quantity) {
        this.variantId = variantId;
        this.product = product;
        this.size = size;
        this.color = color;
        this.quantity = quantity;
    }
}

