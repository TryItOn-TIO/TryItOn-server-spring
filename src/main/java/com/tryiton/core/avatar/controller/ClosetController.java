package com.tryiton.core.avatar.controller;

import com.tryiton.core.auth.security.CustomUserDetails;
import com.tryiton.core.avatar.dto.AvatarProductInfoDto;
import com.tryiton.core.avatar.dto.ClosetPageResponse;
import com.tryiton.core.avatar.service.AvatarService;
import com.tryiton.core.closet.dto.ClosetAddRequest;
import com.tryiton.core.closet.dto.ClosetResponse;
import com.tryiton.core.closet.service.ClosetService;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/closet")
public class ClosetController {

    private final AvatarService avatarService;
    private final WishlistService wishlistService;
    private final ClosetService closetService;

    // 옷장 페이지 데이터 조회
    @GetMapping
    public ResponseEntity<ClosetPageResponse> getClosetPage(
        @AuthenticationPrincipal() CustomUserDetails customUserDetails
    ) {
        Member user = customUserDetails.getUser();

        AvatarProductInfoDto latestAvatar = avatarService.getLatestAvatarWithProducts(user.getId());
        List<ClosetResponse> closetItems = closetService.getClosetItems(user);
        List<ProductResponseDto> wishlistProducts = wishlistService.getWishlistProducts(user);

        return ResponseEntity.ok(
            new ClosetPageResponse(latestAvatar, closetItems, wishlistProducts));
    }

    // 옷장에 추가
    @PostMapping
    public ResponseEntity<Void> addToCloset(
        @AuthenticationPrincipal() CustomUserDetails customUserDetails,
        @RequestBody ClosetAddRequest request
    ) {
        closetService.addToCloset(customUserDetails.getUser(), request.getAvatarId());
        return ResponseEntity.ok().build();
    }

    // 옷장에서 삭제
    @DeleteMapping("/{closetId}")
    public ResponseEntity<Void> removeFromCloset(
        @AuthenticationPrincipal() CustomUserDetails customUserDetails,
        @PathVariable Long closetId
    ) {
        closetService.removeFromCloset(customUserDetails.getUser(), closetId);
        return ResponseEntity.ok().build();
    }
}
