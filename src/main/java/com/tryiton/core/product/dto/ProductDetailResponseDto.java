package com.tryiton.core.product.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tryiton.core.product.entity.Product;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

@Getter
public class ProductDetailResponseDto {

    private final Long id;
    private final String productName;
    private final String brand;
    private final int price; // 정가
    private final int sale; // 할인율 (%)
    private final int salePrice; // 할인된 가격
    private final String content;
    private final List<String> images;
    private final int wishlistCount;
    private final List<ProductVariantDto> variant;
    private final boolean liked; // 찜 여부 추가

    public ProductDetailResponseDto(Product product, List<ProductVariantDto> variant, boolean liked) {
        this.id = product.getId();
        this.productName = product.getProductName();
        this.brand = product.getBrand();
        this.price = product.getPrice();
        this.sale = product.getSale();
        this.content = product.getContent();
        this.wishlistCount = product.getWishlistCount();
        this.variant = variant;
        this.liked = liked;
        
        // 할인된 가격 계산
        if (product.getSale() > 0) {
            this.salePrice = (int) Math.round(product.getPrice() * (100.0 - product.getSale()) / 100.0);
        } else {
            this.salePrice = product.getPrice(); // 할인이 없으면 정가와 동일
        }

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
        if (product.getImg5() != null && !product.getImg5().isEmpty()) {
            try {
                // JSON 문자열을 배열로 파싱
                ObjectMapper objectMapper = new ObjectMapper();
                String[] img5Array = objectMapper.readValue(product.getImg5(), String[].class);

                // 파싱된 배열의 각 URL을 images 리스트에 추가
                for (String imgUrl : img5Array) {
                    if (imgUrl != null && !imgUrl.isEmpty()) {
                        images.add(imgUrl);
                    }
                }
            } catch (Exception e) {
                // JSON 파싱 실패 시 로그 출력하고 원본 문자열 추가
                System.err.println("img5 JSON 파싱 실패: " + e.getMessage());
                images.add(product.getImg5());
            }
        }
    }
}
