package com.tryiton.core.closet.service;

import com.tryiton.core.avatar.dto.AvatarProductInfoDto;
import com.tryiton.core.avatar.service.AvatarService;
import com.tryiton.core.closet.dto.ClosetResponseDto;
import com.tryiton.core.closet.dto.WishlistPageResponseDto;
import com.tryiton.core.member.entity.Member;
import com.tryiton.core.product.dto.ProductResponseDto;
import com.tryiton.core.wishlist.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClosetService {

    private final AvatarService avatarService;
    private final WishlistService wishlistService;

    /**
     * 옷장 메인 데이터 조회 (현재 착장 + 저장된 착장 + 찜 목록)
     */
    @Transactional(readOnly = true)
    public ClosetResponseDto getClosetData(Member member) {
        // 현재 착장 (최신 아바타)
        AvatarProductInfoDto currentOutfit = avatarService.getLatestAvatarWithProducts(member.getId());
        
        // 저장된 착장들 (북마크된 아바타들)
        List<AvatarProductInfoDto> savedOutfits = avatarService.getBookmarkedAvatarsWithProducts(member.getId());
        
        // 찜 목록 (최신 몇 개만)
        List<ProductResponseDto> wishlistProducts = wishlistService.getWishlistProducts(member);
        
        return ClosetResponseDto.builder()
                .currentOutfit(currentOutfit)
                .savedOutfits(savedOutfits)
                .wishlistProducts(wishlistProducts)
                .wishlistCount(wishlistProducts.size())
                .build();
    }

    /**
     * 찜 목록 페이징 조회 (카테고리 필터링 포함)
     */
    @Transactional(readOnly = true)
    public WishlistPageResponseDto getWishlistWithPaging(Member member, Pageable pageable, Long categoryId) {
        List<ProductResponseDto> allWishlistProducts = wishlistService.getWishlistProducts(member);
        
        // 카테고리 필터링
        List<ProductResponseDto> filteredProducts = categoryId != null 
            ? allWishlistProducts.stream()
                .filter(product -> product.getCategoryId().equals(categoryId))
                .toList()
            : allWishlistProducts;
        
        // 페이징 처리
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), filteredProducts.size());
        List<ProductResponseDto> pagedProducts = filteredProducts.subList(start, end);
        
        Page<ProductResponseDto> page = new PageImpl<>(pagedProducts, pageable, filteredProducts.size());
        
        return WishlistPageResponseDto.from(page);
    }

    /**
     * 착장 북마크 토글
     */
    @Transactional
    public void toggleOutfitBookmark(Member member, Long avatarId, boolean bookmark) {
        avatarService.updateBookmark(avatarId, member.getId(), bookmark);
    }

    /**
     * 찜 추가
     */
    @Transactional
    public void addToWishlist(Member member, Long productId) {
        wishlistService.addProductToWishlist(member, productId);
    }

    /**
     * 찜 제거
     */
    @Transactional
    public void removeFromWishlist(Member member, Long productId) {
        wishlistService.removeProductFromWishlist(member, productId);
    }
}
