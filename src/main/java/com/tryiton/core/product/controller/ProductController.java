package com.tryiton.core.product.controller;

import com.tryiton.core.auth.security.CustomUserDetails;
import com.tryiton.core.product.dto.CategoryProductResponse;
import com.tryiton.core.product.dto.ProductResponseDto;
import com.tryiton.core.product.dto.SearchProductResponse;
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

    // 검색 기능
    @GetMapping("/suggestions")
    public ResponseEntity<List<String>> getSearchSuggestions(@RequestParam String query) {
        List<String> suggestions = productService.getSearchSuggestions(query);
        return ResponseEntity.ok(suggestions);
    }

    @GetMapping("/search")
    public ResponseEntity<SearchProductResponse> searchProducts(
        @RequestParam String query,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = (userDetails != null) ? userDetails.getUser().getId() : null;
        SearchProductResponse result = productService.searchProducts(query, userId, page, size);
        return ResponseEntity.ok(result);
    }
}
