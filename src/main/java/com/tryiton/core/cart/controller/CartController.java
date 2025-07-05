package com.tryiton.core.cart.controller;

import com.tryiton.core.auth.security.SecurityUtil;
import com.tryiton.core.cart.dto.CartAddRequestDto;
import com.tryiton.core.cart.dto.CartItemDto;
import com.tryiton.core.cart.dto.CartItemUpdateRequestDto;
import com.tryiton.core.cart.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<List<CartItemDto>> getCartItems() {
        Long userId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(cartService.getCartItems(userId));
    }

    @PostMapping("/items")
    public ResponseEntity<Void> addItemToCart(@RequestBody CartAddRequestDto requestDto) {
        Long userId = SecurityUtil.getCurrentUserId();
        cartService.addOrUpdateItem(userId, requestDto);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/items/{cartItemId}")
    public ResponseEntity<Void> updateCartItemQuantity(
            @PathVariable Long cartItemId,
            @RequestBody CartItemUpdateRequestDto requestDto) {
        Long userId = SecurityUtil.getCurrentUserId();
        cartService.updateItemQuantity(userId, cartItemId, requestDto.getQuantity());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<Void> deleteCartItem(@PathVariable Long cartItemId) {
        Long userId = SecurityUtil.getCurrentUserId();
        cartService.deleteCartItem(userId, cartItemId);
        return ResponseEntity.noContent().build();
    }
}