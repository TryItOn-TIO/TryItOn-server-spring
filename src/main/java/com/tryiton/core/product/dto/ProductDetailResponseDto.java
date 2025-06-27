package com.tryiton.core.product.dto;

import com.tryiton.core.product.entity.Product;
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
    private final String img1;
    private final String img2;
    private final String img3;
    private final String img4;
    private final String img5;
    private final int wishlistCount;
    private final List<ProductVariantDto> variant;

    public ProductDetailResponseDto(Product product, List<ProductVariantDto> variant) {
        this.id = product.getId();
        this.productName = product.getProductName();
        this.brand = product.getBrand();
        this.price = product.getPrice();
        this.sale = product.getSale();
        this.content = product.getContent();
        this.img1 = product.getImg1();
        this.img2 = product.getImg2();
        this.img3 = product.getImg3();
        this.img4 = product.getImg4();
        this.img5 = product.getImg5();
        this.wishlistCount = product.getWishlistCount();
        this.variant = variant;
    }
}
