package com.tryiton.core.product.controller;

import com.tryiton.core.auth.security.CustomUserDetails;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
        @AuthenticationPrincipal() CustomUserDetails customUserDetails
    ) {
        // 인증된 사용자만 접근 가능 (기존 로직 유지)
        Long userId = customUserDetails.getUser().getId();

        List<ProductResponseDto> recommended = productService.getPersonalizedRecommendations(
            userId);
        List<ProductResponseDto> ranked = productService.getTopRankedProducts(userId);
        AvatarProductInfoDto avatarInfo = avatarService.getLatestAvatarWithProducts(userId);

        return ResponseEntity.ok(MainProductResponse.builder()
            .recommended(recommended)
            .ranked(ranked)
            .avatarInfo(avatarInfo)
            .build());
    }

    @GetMapping("/category")
    public ResponseEntity<CategoryProductResponse> getCategoryProducts(
        @AuthenticationPrincipal() CustomUserDetails customUserDetails,
        @RequestParam Long categoryId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        // 비로그인 사용자도 접근 가능하도록 수정
        Long userId = (customUserDetails != null) ? customUserDetails.getUser().getId() : null;

        Category category = categoryService.findByIdWithChildren(categoryId);
        Page<ProductResponseDto> products = productService.getProductsByCategory(userId, category,
            page,
            size);

        return ResponseEntity.ok(new CategoryProductResponse(products));
    }
}
