package com.tryiton.core.wishlist.service;

import com.tryiton.core.member.entity.Member;
import com.tryiton.core.member.repository.MemberRepository;
import com.tryiton.core.product.dto.ProductResponseDto;
import com.tryiton.core.product.entity.Product;
import com.tryiton.core.product.repository.ProductRepository;
import com.tryiton.core.wishlist.entity.Wishlist;
import com.tryiton.core.wishlist.entity.WishlistItem;
import com.tryiton.core.wishlist.repository.WishlistItemRepository;
import com.tryiton.core.wishlist.repository.WishlistRepository;
import java.util.List;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final WishlistItemRepository wishlistItemRepository;
    private final ProductRepository productRepository;
    private final MemberRepository memberRepository;

    // 찜 추가 (중복 찜 방지 로직 추가)
    @Transactional
    public void addProductToWishlist(Long userId, Long productId) {
        Member user = memberRepository.findById(userId)
            .orElseThrow(() -> new NoSuchElementException("존재하지 않는 유저입니다."));

        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new NoSuchElementException("존재하지 않는 상품입니다."));

        Wishlist wishlist = wishlistRepository.findByUser(user)
            .orElseGet(() -> wishlistRepository.save(Wishlist.builder().user(user).build()));

        boolean alreadyExists = wishlist.getItems().stream()
            .anyMatch(item -> item.getProduct().getId().equals(productId));

        if (!alreadyExists) {
            WishlistItem item = WishlistItem.builder().product(product).build();
            wishlist.addItem(item);
            product.increaseWishlistCount(); // 찜 수 증가
        }
    }

    // 찜 제거
    @Transactional
    public void removeProductFromWishlist(Long userId, Long productId) {
        Wishlist wishlist = wishlistRepository.findByUserId(userId)
            .orElseThrow(() -> new NoSuchElementException("찜 목록이 존재하지 않습니다."));

        WishlistItem item = wishlist.getItems().stream()
            .filter(i -> i.getProduct().getId().equals(productId))
            .findFirst()
            .orElseThrow(() -> new NoSuchElementException("찜 목록에 없는 상품입니다."));

        wishlist.removeItem(item);
        wishlistItemRepository.delete(item);
        item.getProduct().decreaseWishlistCount(); // 찜 수 감소
    }

    // 찜 조회
    @Transactional(readOnly = true)
    public List<ProductResponseDto> getWishlistProducts(Long userId) {
        Wishlist wishlist = wishlistRepository.findByUserId(userId)
            .orElseThrow(() -> new NoSuchElementException("찜 목록이 존재하지 않습니다."));

        List<WishlistItem> sortedItems = wishlistItemRepository.findAllByWishlist_WishlistIdOrderByCreatedAtDesc(
            wishlist.getWishlistId());

        return sortedItems.stream()
            .map(item -> new ProductResponseDto(item.getProduct(), true))
            .toList();
    }
}
