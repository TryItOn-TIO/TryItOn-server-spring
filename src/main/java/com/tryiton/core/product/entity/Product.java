package com.tryiton.core.product.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.HashSet;
import java.util.Set;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    @JsonProperty("product_id")  // Lambda 응답의 product_id와 매핑
    private Long id;

    @CreatedDate
    @Column(name = "create_at", updatable = false)
    @JsonProperty("create_at")  // Lambda 응답의 create_at과 매핑
    private LocalDateTime createAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(nullable = false, length = 150)
    @JsonProperty("product_name")  // Lambda 응답의 product_name과 매핑
    private String productName;

    @Column(nullable = false, length = 600)
    private String img1;

    private String img2;
    private String img3;
    private String img4;

    @Column(columnDefinition = "LONGTEXT")
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
        String img4, String img5, String content, int price, int sale, String brand, String gender, Long id) {
        this.id = id;
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

    /**
     * 이 상품이 상의인지 확인합니다. (부모 카테고리 ID: 1 또는 2)
     * @return 상의이면 true, 아니면 false
     */
    public boolean isUpperGarment() {
        return this.category != null && this.category.getParentCategory() != null && (this.category.getParentCategory().getId() == 1 || this.category.getParentCategory().getId() == 2);
    }

    /**
     * 이 상품이 하의인지 확인합니다. (부모 카테고리 ID: 3 또는 스커트 카테고리)
     * 스커트(404, 405, 406)는 하의로 인식하되, 원피스(401, 402, 403)는 제외합니다.
     * @return 하의이면 true, 아니면 false
     */
    public boolean isLowerGarment() {
        // 부모 카테고리가 3인 경우 (바지류)
        if (this.category != null && this.category.getParentCategory() != null && 
            this.category.getParentCategory().getId() == 3) {
            return true;
        }
        
        // 부모 카테고리가 4인 경우 (원피스/스커트류)
        if (this.category != null && this.category.getParentCategory() != null && 
            this.category.getParentCategory().getId() == 4) {
            
            // 스커트 카테고리만 하의로 인식 (카테고리 ID: 404, 405, 406)
            Long categoryId = this.category.getId();
            
            // 스커트 카테고리 ID 목록
            return categoryId != null && (
                categoryId == 404 || // 미니스커트
                categoryId == 405 || // 미디스커트
                categoryId == 406    // 롱스커트
            );
        }
        
        return false;
    }
    
    /**
     * 이 상품이 원피스인지 확인합니다. (카테고리 ID: 401, 402, 403)
     * 원피스는 상의와 하의가 결합된 특수한 의류이므로 별도로 처리
     * @return 원피스이면 true, 아니면 false
     */
    public boolean isDress() {
        if (this.category != null && this.category.getParentCategory() != null && 
            this.category.getParentCategory().getId() == 4) {
            
            Long categoryId = this.category.getId();
            
            return categoryId != null && (
                categoryId == 401 || // 미니원피스
                categoryId == 402 || // 미디원피스
                categoryId == 403    // 맥시원피스
            );
        }
        
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Product product)) return false;
        if (this.id == null || product.id == null) return false;
        return this.id.equals(product.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
