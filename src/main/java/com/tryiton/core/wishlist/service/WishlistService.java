package com.tryiton.core.wishlist.service;

import com.tryiton.core.common.enums.RecommendAction;
import com.tryiton.core.common.exception.BusinessException;
import com.tryiton.core.member.entity.Member;
import com.tryiton.core.product.dto.ProductResponseDto;
import com.tryiton.core.product.entity.Product;
import com.tryiton.core.product.repository.ProductRepository;
import com.tryiton.core.recommend.service.RecommendBehaviorLogService;
import com.tryiton.core.wishlist.entity.Wishlist;
import com.tryiton.core.wishlist.entity.WishlistItem;
import com.tryiton.core.wishlist.repository.WishlistItemRepository;
import com.tryiton.core.wishlist.repository.WishlistRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final WishlistItemRepository wishlistItemRepository;
    private final ProductRepository productRepository;

    private final RecommendBehaviorLogService recommendBehaviorLogService;

    // 찜 추가 (중복 찜 방지 로직 추가)
    @Transactional
    public void addProductToWishlist(Member user, Long productId) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "존재하지 않는 상품입니다."));

        Wishlist wishlist = wishlistRepository.findByUser(user)
            .orElseGet(() -> wishlistRepository.save(Wishlist.builder().user(user).build()));

        boolean alreadyExists = wishlist.getItems().stream()
            .anyMatch(item -> item.getProduct().getId().equals(productId));

        if (!alreadyExists) {
            WishlistItem item = WishlistItem.builder().product(product).build();
            wishlist.addItem(item);
            product.increaseWishlistCount(); // 찜 수 증가
        }

        // 유저 행동 로그 비동기 기록
        recommendBehaviorLogService.logUserAction(user.getId(), productId,
            RecommendAction.WISHLIST);
    }

    // 찜 제거
    @Transactional
    public void removeProductFromWishlist(Member user, Long productId) {
        Wishlist wishlist = wishlistRepository.findByUserId(user.getId())
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "찜 목록이 존재하지 않습니다."));

        WishlistItem item = wishlist.getItems().stream()
            .filter(i -> i.getProduct().getId().equals(productId))
            .findFirst()
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "찜 목록에 없는 상품입니다."));

        wishlist.removeItem(item);
        wishlistItemRepository.delete(item);
        item.getProduct().decreaseWishlistCount(); // 찜 수 감소
    }

    // 찜 조회
    @Transactional(readOnly = true)
    public List<ProductResponseDto> getWishlistProducts(Member user) {
        Wishlist wishlist = wishlistRepository.findByUserId(user.getId())
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "찜 목록이 존재하지 않습니다."));

        // N+1 쿼리 해결: WishlistItem과 Product를 함께 조회
        List<WishlistItem> sortedItems = wishlistItemRepository.findAllByWishlistIdWithProductOrderByCreatedAtDesc(
            wishlist.getWishlistId());

        return sortedItems.stream()
            .map(item -> new ProductResponseDto(item.getProduct(), true)) // 이미 fetch join으로 로딩됨
            .toList();
    }

    // 특정 카테고리에 해당하는 찜한 상품 조회
    @Transactional(readOnly = true)
    public List<ProductResponseDto> getWishlistProductsByParentCategory(Member user,
        Long parentCategoryId) {
        List<WishlistItem> items = wishlistItemRepository
            .findByUserIdAndProductParentCategoryIdWithProduct(user.getId(), parentCategoryId);

        return items.stream()
            .map(item -> new ProductResponseDto(item.getProduct(), true))
            .toList();
    }
}
