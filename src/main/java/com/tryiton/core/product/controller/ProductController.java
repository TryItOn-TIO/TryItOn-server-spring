package com.tryiton.core.product.controller;

import com.tryiton.core.product.dto.MainProductResponse;
import com.tryiton.core.product.dto.ProductResponseDto;
import com.tryiton.core.product.entity.Category;
import com.tryiton.core.product.service.CategoryService;
import com.tryiton.core.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/home/products")
public class ProductController {

    private final ProductService productService;
    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<MainProductResponse> getMainProducts(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        List<ProductResponseDto> recommended = productService.getPersonalizedRecommendations(userId);
        Page<ProductResponseDto> ranked = productService.getTopRankedProducts(page, size);
        return ResponseEntity.ok(new MainProductResponse(recommended, ranked));
    }

    @GetMapping("/category")
    public ResponseEntity<Page<ProductResponseDto>> getCategoryProducts(
            @RequestParam Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Category category = categoryService.findById(categoryId);
        Page<ProductResponseDto> products = productService.getProductsByCategory(category, page, size);
        return ResponseEntity.ok(products);
    }
}