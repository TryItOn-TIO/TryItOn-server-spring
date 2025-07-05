package com.tryiton.core.mypage.controller;

import com.tryiton.core.auth.security.CustomUserDetails;
import com.tryiton.core.mypage.dto.*;
import com.tryiton.core.mypage.service.MyPageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mypage")
@RequiredArgsConstructor
public class MyPageController {

    private final MyPageService myPageService;

    @GetMapping("/profile")
    public ResponseEntity<ProfileResponseDto> getMyProfile(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(myPageService.getProfile(userDetails.getUser().getId()));
    }

    @PutMapping("/profile")
    public ResponseEntity<Void> updateMyProfile(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody ProfileUpdateRequestDto requestDto) {
        myPageService.updateProfile(userDetails.getUser().getId(), requestDto);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/orders")
    public ResponseEntity<Page<OrderHistoryDto>> getMyOrderHistory(@AuthenticationPrincipal CustomUserDetails userDetails, @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(myPageService.getOrderHistory(userDetails.getUser().getId(), pageable));
    }
}
