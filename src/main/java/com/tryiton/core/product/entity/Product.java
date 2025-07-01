package com.tryiton.core.product.entity;

import com.tryiton.core.common.BaseTimeEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(nullable = false, length = 150)
    private String productName;

    @Column(nullable = false, length = 600)
    private String img1;

    private String img2;
    private String img3;
    private String img4;
    private String img5;

    @Column(length = 1500)
    private String content;

    private int price;
    private int sale;

    @Column(nullable = false, length = 150)
    private String brand;

    private boolean deleted;

    @Column(name = "wishlist_count")
    private int wishlistCount;

     private String gender;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<ProductVariant> variants = new ArrayList<>();

    @Builder
    public Product(Category category, String productName, String img1, String img2, String img3,
        String img4, String img5, String content, int price, int sale, String brand, String gender) {
        this.category = category;
        this.productName = productName;
        this.img1 = img1;
        this.img2 = img2;
        this.img3 = img3;
        this.img4 = img4;
        this.img5 = img5;
        this.content = content;
        this.price = price;
        this.sale = sale;
        this.brand = brand;
        this.deleted = false;
        this.wishlistCount = 0;
        this.gender = gender;
    }

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "product_tag", // 실제 DB의 연결 테이블 이름
        joinColumns = @JoinColumn(name = "product_id"), // 이 엔티티(Product)를 참조하는 외래 키
        inverseJoinColumns = @JoinColumn(name = "tag_id") // 상대 엔티티(Tag)를 참조하는 외래 키
    )
    private Set<Tag> tags = new HashSet<>();

    public void increaseWishlistCount() {
        this.wishlistCount++;
    }

    public void decreaseWishlistCount() {
        if (this.wishlistCount > 0) {
            this.wishlistCount--;
        }
    }

    public static boolean isUpperGarment(Product garment) {
        if (garment.category.getParentCategory().getId() == 1) {
            return true;
        }
        return false;
    }
}
