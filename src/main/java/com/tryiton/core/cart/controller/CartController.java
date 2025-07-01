package com.tryiton.core.cart.controller;

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
        Long userId = 1L; // TODO: SecurityContext에서 사용자 ID 가져오기
        return ResponseEntity.ok(cartService.getCartItems(userId));
    }

    @PostMapping("/items")
    public ResponseEntity<Void> addItemToCart(@RequestBody CartAddRequestDto requestDto) {
        Long userId = 1L; // TODO: SecurityContext에서 사용자 ID 가져오기
        cartService.addOrUpdateItem(userId, requestDto);
        return ResponseEntity.ok().build();
    }

    // ★★★ [신규] 수량 변경 API ★★★
    @PutMapping("/items/{cartItemId}")
    public ResponseEntity<Void> updateCartItemQuantity(
            @PathVariable Long cartItemId,
            @RequestBody CartItemUpdateRequestDto requestDto) {
        cartService.updateItemQuantity(cartItemId, requestDto.getQuantity());
        return ResponseEntity.ok().build();
    }

    // ★★★ [신규] 삭제 API ★★★
    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<Void> deleteCartItem(@PathVariable Long cartItemId) {
        cartService.deleteCartItem(cartItemId);
        return ResponseEntity.noContent().build(); // 내용 없이 성공(204) 응답
    }
}