package com.tryiton.core.product.controller;

import com.tryiton.core.avatar.dto.AvatarProductInfoDto;
import com.tryiton.core.avatar.service.AvatarService;
import com.tryiton.core.product.dto.CategoryProductResponse;
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
    private final AvatarService avatarService;

    @GetMapping
    public ResponseEntity<MainProductResponse> getMainProducts(
        @RequestParam Long userId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        List<ProductResponseDto> recommended = productService.getPersonalizedRecommendations(
            userId);
        Page<ProductResponseDto> ranked = productService.getTopRankedProducts(userId, page, size);
        AvatarProductInfoDto avatarInfo = avatarService.getLatestAvatarWithProducts(userId);

        return ResponseEntity.ok(MainProductResponse.builder()
            .recommended(recommended)
            .ranked(ranked)
            .avatarInfo(avatarInfo)
            .build());
    }

    @GetMapping("/category")
    public ResponseEntity<CategoryProductResponse> getCategoryProducts(
        @RequestParam Long userId,
        @RequestParam Long categoryId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        Category category = categoryService.findById(categoryId);
        Page<ProductResponseDto> products = productService.getProductsByCategory(userId, category,
            page,
            size);
        AvatarProductInfoDto avatarInfo = avatarService.getLatestAvatarWithProducts(userId);

        return ResponseEntity.ok(new CategoryProductResponse(products, avatarInfo));
    }
}