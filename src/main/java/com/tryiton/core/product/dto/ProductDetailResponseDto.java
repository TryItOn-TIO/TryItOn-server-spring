package com.tryiton.core.product.dto;

import com.tryiton.core.product.entity.Product;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

@Getter
public class ProductDetailResponseDto {

    private final Long id;
    private final String productName;
    private final String brand;
    private final int price;
    private final int sale;
    private final String content;
    private final List<String> images;
    private final int wishlistCount;
    private final List<ProductVariantDto> variant;

    public ProductDetailResponseDto(Product product, List<ProductVariantDto> variant) {
        this.id = product.getId();
        this.productName = product.getProductName();
        this.brand = product.getBrand();
        this.price = product.getPrice();
        this.sale = product.getSale();
        this.content = product.getContent();
        this.wishlistCount = product.getWishlistCount();
        this.variant = variant;

        // 이미지 필드를 List로 구성
        this.images = new ArrayList<>();

        if (product.getImg1() == null) {
            throw new IllegalArgumentException("img1은 반드시 존재해야 합니다.");
        }
        images.add(product.getImg1()); // img1은 무조건 존재해야함
        if (product.getImg2() != null) {
            images.add(product.getImg2());
        }
        if (product.getImg3() != null) {
            images.add(product.getImg3());
        }
        if (product.getImg4() != null) {
            images.add(product.getImg4());
        }
        if (product.getImg5() != null) {
            images.add(product.getImg5());
        }
    }
}
