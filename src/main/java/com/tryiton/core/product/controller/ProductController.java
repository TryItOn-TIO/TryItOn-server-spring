package com.tryiton.core.product.controller;

import com.tryiton.core.product.dto.MainProductResponse;
import com.tryiton.core.product.dto.ProductResponseDto;
import com.tryiton.core.product.service.ProductService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/home/products")
public class ProductController {

    private final ProductService productService;

    // 메인 탭 조회 : 추천 상품 + 랭킹 상품
    @GetMapping
    public MainProductResponse getMainProducts(
        @RequestParam List<Long> recommendedIds,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        List<ProductResponseDto> recommended = productService.getRecommendedProducts(
            recommendedIds);
        Page<ProductResponseDto> ranked = productService.getTopRankedProducts(page, size);

        return new MainProductResponse(recommended, ranked);
    }
}
