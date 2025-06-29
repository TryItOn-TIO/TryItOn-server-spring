package com.tryiton.core.wishlist.controller;

import com.tryiton.core.product.dto.ProductResponseDto;
import com.tryiton.core.wishlist.service.WishlistService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/wishlist")
public class WishlistController {

    private final WishlistService wishlistService;

    // 찜하기 추가
    @PostMapping("/add")
    public void addProductToWishlist(
        @RequestParam Long userId,
        @RequestParam Long productId
    ) {
        wishlistService.addProductToWishlist(userId, productId);
    }

    // 찜하기 제거
    @DeleteMapping("/remove")
    public void removeProductFromWishlist(
        @RequestParam Long userId,
        @RequestParam Long productId
    ) {
        wishlistService.removeProductFromWishlist(userId, productId);
    }

    // 찜한 상품 목록 조회
    @GetMapping
    public List<ProductResponseDto> getWishlist(@RequestParam Long userId) {
        return wishlistService.getWishlistProducts(userId);
    }
}
