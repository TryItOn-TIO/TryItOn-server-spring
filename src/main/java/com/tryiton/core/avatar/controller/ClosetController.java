package com.tryiton.core.avatar.controller;

import com.tryiton.core.avatar.dto.AvatarProductInfoDto;
import com.tryiton.core.avatar.dto.ClosetPageResponse;
import com.tryiton.core.avatar.service.AvatarService;
import com.tryiton.core.product.dto.ProductResponseDto;
import com.tryiton.core.wishlist.service.WishlistService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/closet")
public class ClosetController {

    private final AvatarService avatarService;
    private final WishlistService wishlistService;

    // 옷장 페이지 데이터 조회
    @GetMapping
    public ResponseEntity<ClosetPageResponse> getClosetPage(@RequestParam Long userId) {
        AvatarProductInfoDto latestAvatar = avatarService.getLatestAvatarWithProducts(userId);
        List<AvatarProductInfoDto> bookmarkedAvatars = avatarService.getBookmarkedAvatarsWithProducts(
            userId);
        List<ProductResponseDto> wishlistProducts = wishlistService.getWishlistProducts(userId);

        return ResponseEntity.ok(
            new ClosetPageResponse(latestAvatar, bookmarkedAvatars, wishlistProducts));
    }

    // 북마크 추가
    @PostMapping("/{avatarId}/bookmark")
    public ResponseEntity<Void> addBookmark(
        @PathVariable Long avatarId,
        @RequestParam Long userId
    ) {
        avatarService.updateBookmark(avatarId, userId, true);
        return ResponseEntity.ok().build();
    }

    // 북마크 해제
    @DeleteMapping("/{avatarId}/bookmark")
    public ResponseEntity<Void> removeBookmark(
        @PathVariable Long avatarId,
        @RequestParam Long userId
    ) {
        avatarService.updateBookmark(avatarId, userId, false);
        return ResponseEntity.ok().build();
    }
}
