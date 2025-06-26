package com.tryiton.core.avatar.entity;

import com.tryiton.core.product.entity.Product;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class AvatarItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 식별 관계: Avatar가 식별자 역할
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "avatar_id", nullable = false)
    private Avatar avatar;

    // 일반 외래키 관계
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Builder
    public AvatarItem(Avatar avatar, Product product) {
        this.avatar = avatar;
        this.product = product;
    }
}
