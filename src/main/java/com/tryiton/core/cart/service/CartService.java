package com.tryiton.core.cart.service;

import com.tryiton.core.cart.dto.CartAddRequestDto;
import com.tryiton.core.cart.dto.CartItemDto;
import com.tryiton.core.cart.entity.Cart;
import com.tryiton.core.cart.entity.CartItem;
import com.tryiton.core.cart.repository.CartRepository;
import com.tryiton.core.cart.repository.CartItemRepository;
import com.tryiton.core.common.exception.BusinessException;
import com.tryiton.core.member.entity.Member;
import com.tryiton.core.member.repository.MemberRepository;
import com.tryiton.core.product.entity.ProductVariant;
import com.tryiton.core.product.repository.ProductVariantRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final MemberRepository memberRepository;
    private final ProductVariantRepository productVariantRepository;
    private final CartItemRepository cartItemRepository;

    @Transactional(readOnly = true)
    public List<CartItemDto> getCartItems(Long userId) {
        Cart cart = findCartByUserId(userId);
        return cart.getCartItems().stream()
                .map(CartItemDto::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public void addOrUpdateItem(Long userId, CartAddRequestDto requestDto) {
        Cart cart = findOrCreateCart(userId);
        ProductVariant variant = productVariantRepository.findById(requestDto.getVariantId())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "상품 옵션을 찾을 수 없습니다."));

        // 이미 장바구니에 같은 상품 옵션이 있는지 확인
        cart.getCartItems().stream()
                .filter(item -> item.getVariant().getVariantId().equals(requestDto.getVariantId()))
                .findFirst()
                .ifPresentOrElse(
                        item -> item.updateQuantity(item.getQuantity() + requestDto.getQuantity()), // 있으면 수량 추가
                        () -> { // 없으면 새로 추가
                            CartItem newCartItem = CartItem.builder()
                                    .cart(cart).variant(variant).quantity(requestDto.getQuantity()).build();
                            cart.getCartItems().add(newCartItem);
                        }
                );
    }
    // ★★★ [신규] 수량 변경 로직 ★★★
    @Transactional
    public void updateItemQuantity(Long cartItemId, int quantity) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "장바구니 상품을 찾을 수 없습니다."));
        cartItem.updateQuantity(quantity);
    }

    // ★★★ [신규] 삭제 로직 ★★★
    @Transactional
    public void deleteCartItem(Long cartItemId) {
        // orphanRemoval=true 옵션 덕분에 Cart에서 CartItem을 제거하면 DB에서도 삭제됩니다.
        // 또는, cartItemRepository.deleteById(cartItemId); 를 직접 호출해도 됩니다.
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "장바구니 상품을 찾을 수 없습니다."));
        cartItem.getCart().getCartItems().remove(cartItem);
    }

    private Cart findCartByUserId(Long userId) {
        return cartRepository.findByMemberId(userId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "장바구니 상품을 찾을 수 없습니다."));
    }

    private Cart findOrCreateCart(Long userId) {
        return cartRepository.findByMemberId(userId).orElseGet(() -> {
            Member member = memberRepository.findById(userId)
                    .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));
            return cartRepository.save(new Cart(member));
        });
    }
}
