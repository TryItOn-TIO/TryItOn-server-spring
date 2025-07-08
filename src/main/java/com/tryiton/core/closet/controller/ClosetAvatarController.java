package com.tryiton.core.closet.controller;

import com.tryiton.core.auth.security.CustomUserDetails;
import com.tryiton.core.closet.dto.ClosetAvatarResponseDto;
import com.tryiton.core.closet.service.ClosetAvatarService;
import com.tryiton.core.member.entity.Member;
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
public class ClosetAvatarController {

    private final ClosetAvatarService closetAvatarService;

    // 옷장 목록 조회
    @GetMapping
    public ResponseEntity<List<ClosetAvatarResponseDto>> getCloset(
        @AuthenticationPrincipal CustomUserDetails userDetails) {
        Member member = userDetails.getUser();
        List<ClosetAvatarResponseDto> response = closetAvatarService.getClosetAvatarByUser(
            member.getId());
        return ResponseEntity.ok(response);
    }

    // 아바타 착장 저장
    @PostMapping
    public ResponseEntity<Boolean> saveClosetAvatar(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Member member = userDetails.getUser();
        closetAvatarService.saveClosetAvatar(member);
        return ResponseEntity.ok(true);
//        return ResponseEntity.status(HttpStatus.CREATED).build(); // 201 Created
    }

    // 착장 삭제
    @DeleteMapping("/{closetAvatarId}")
    public ResponseEntity<Void> deleteClosetAvatar(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @PathVariable Long closetAvatarId) {
        Member member = userDetails.getUser();
        closetAvatarService.deleteClosetAvatar(closetAvatarId, member.getId());
        return ResponseEntity.noContent().build(); // 204 No Content
    }
}
