package com.tryiton.core.product.controller;

import com.tryiton.core.auth.security.CustomUserDetails;
import com.tryiton.core.product.dto.ProductDetailResponseDto;
import com.tryiton.core.product.dto.ProductResponseDto;
import com.tryiton.core.product.service.ProductService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductDetailController {

    private final ProductService productService;

    // 상품 상세 조회
    @GetMapping("/{productId}")
    public ProductDetailResponseDto getProductDetail(
        @AuthenticationPrincipal CustomUserDetails customUserDetails,
        @PathVariable Long productId) {

        Long userId = customUserDetails.getUser().getId();
        return productService.getProductDetail(userId, productId);
    }

    // 특정 상품과 유사한 상품 목록 조회(하위 카테고리가 같은 상품 추천)
    @GetMapping("/{productId}/similar")
    public ResponseEntity<List<ProductResponseDto>> getSimilarProducts(
        @AuthenticationPrincipal CustomUserDetails customUserDetails,
        @PathVariable Long productId
    ) {
        Long userId = customUserDetails.getUser().getId();
        List<ProductResponseDto> similarProducts = productService.getSimilarProducts(userId, productId);
        return ResponseEntity.ok(similarProducts);
    }

}
