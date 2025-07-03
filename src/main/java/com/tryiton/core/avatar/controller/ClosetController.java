package com.tryiton.core.avatar.controller;

import com.tryiton.core.auth.security.CustomUserDetails;
import com.tryiton.core.avatar.dto.AvatarProductInfoDto;
import com.tryiton.core.avatar.dto.ClosetPageResponse;
import com.tryiton.core.avatar.service.AvatarService;
import com.tryiton.core.member.entity.Member;
import com.tryiton.core.product.dto.ProductResponseDto;
import com.tryiton.core.wishlist.service.WishlistService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/closet")
public class ClosetController {

    private final AvatarService avatarService;
    private final WishlistService wishlistService;

    // 옷장 페이지 데이터 조회
    @GetMapping
    public ResponseEntity<ClosetPageResponse> getClosetPage(
        @AuthenticationPrincipal() CustomUserDetails customUserDetails
    ) {
        Member user = customUserDetails.getUser();

        AvatarProductInfoDto latestAvatar = avatarService.getLatestAvatarWithProducts(user.getId());
        List<AvatarProductInfoDto> bookmarkedAvatars = avatarService.getBookmarkedAvatarsWithProducts(user.getId());
        List<ProductResponseDto> wishlistProducts = wishlistService.getWishlistProducts(user);

        return ResponseEntity.ok(
            new ClosetPageResponse(latestAvatar, bookmarkedAvatars, wishlistProducts));
    }

    // 북마크 추가
    @PostMapping("/{avatarId}/bookmark")
    public ResponseEntity<Void> addBookmark(
        @AuthenticationPrincipal() CustomUserDetails customUserDetails,
        @PathVariable Long avatarId
    ) {
        avatarService.updateBookmark(avatarId, customUserDetails.getUser().getId(), true);
        return ResponseEntity.ok().build();
    }

    // 북마크 해제
    @DeleteMapping("/{avatarId}/bookmark")
    public ResponseEntity<Void> removeBookmark(
        @AuthenticationPrincipal() CustomUserDetails customUserDetails,
        @PathVariable Long avatarId
    ) {
        avatarService.updateBookmark(avatarId, customUserDetails.getUser().getId(), false);
        return ResponseEntity.ok().build();
    }
}
