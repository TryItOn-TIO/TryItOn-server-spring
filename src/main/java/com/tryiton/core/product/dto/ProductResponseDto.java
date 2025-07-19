package com.tryiton.core.product.dto;

import com.tryiton.core.product.entity.Product;
import com.tryiton.core.recommend.dto.CachedProductDto;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ProductResponseDto {

    private Long id;
    private String productName;
    private String img1;
    private int price; // 정가
    private int sale; // 할인율 (%)
    private int salePrice; // 할인된 가격
    private boolean liked; // 유저가 찜한 상품인지 여부
    private String brand;
    private int wishlistCount;
    private LocalDateTime createdAt;
    private Long categoryId;
    private String categoryName;

    public ProductResponseDto(Product product, boolean liked) {
        this.id = product.getId();
        this.productName = product.getProductName();
        this.img1 = product.getImg1();
        this.price = product.getPrice();
        this.sale = product.getSale();
        this.liked = liked;
        this.brand = product.getBrand();
        this.wishlistCount = product.getWishlistCount();
        this.createdAt = product.getCreateAt();
        this.categoryId = product.getCategory().getId();
        this.categoryName = product.getCategory().getCategoryName();
        
        // ���인된 가격 계산
        if (product.getSale() > 0) {
            this.salePrice = (int) Math.round(product.getPrice() * (100.0 - product.getSale()) / 100.0);
        } else {
            this.salePrice = product.getPrice(); // 할인이 없으면 정가와 동일
        }
    }

    /**
     * 캐시된 상품 정보로부터 응답 DTO를 생성하는 생성자
     * @param cachedProduct Redis에 캐시되어 있던 상품 정보
     * @param liked 현재 사용자의 찜 여부
     */
    public ProductResponseDto(CachedProductDto cachedProduct, boolean liked) {
        this.id = cachedProduct.getProductId();
        this.productName = cachedProduct.getProductName();
        this.img1 = cachedProduct.getImageUrl();
        this.price = cachedProduct.getPrice();
        this.sale = cachedProduct.getSale();
        this.salePrice = cachedProduct.getSalePrice();
        this.liked = liked;
        this.brand = cachedProduct.getBrand();
        this.wishlistCount = (int) cachedProduct.getWishlistCount();
        this.createdAt = cachedProduct.getCreatedAt();
        this.categoryId = cachedProduct.getCategoryId();
        this.categoryName = cachedProduct.getCategoryName();
    }

    public static ProductResponseDto from(Product product, boolean liked) {
        return new ProductResponseDto(product, liked);
    }

    // PersonalizedService와의 호환성을 위해 복원된 생성자
    public ProductResponseDto(Long id, String productName, String img1, int price, int sale,
                             int salePrice, boolean liked, String brand, int wishlistCount,
                             LocalDateTime createdAt, Long categoryId, String categoryName) {
        this.id = id;
        this.productName = productName;
        this.img1 = img1;
        this.price = price;
        this.sale = sale;
        this.salePrice = salePrice;
        this.liked = liked;
        this.brand = brand;
        this.wishlistCount = wishlistCount;
        this.createdAt = createdAt;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
    }
}
