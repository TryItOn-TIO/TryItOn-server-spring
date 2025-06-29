package com.tryiton.core.product.controller;

import com.tryiton.core.product.dto.ProductDetailResponseDto;
import com.tryiton.core.product.service.ProductService;
import lombok.RequiredArgsConstructor;
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
    public ProductDetailResponseDto getProductDetail(@PathVariable Long productId) {
        return productService.getProductDetail(productId);
    }
}
