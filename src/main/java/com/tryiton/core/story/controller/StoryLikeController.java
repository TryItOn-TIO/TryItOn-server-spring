package com.tryiton.core.story.controller;

import com.tryiton.core.auth.security.CustomUserDetails;
import com.tryiton.core.story.service.StoryLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stories")
@RequiredArgsConstructor
public class StoryLikeController {

    private final StoryLikeService storyLikeService;

    @PostMapping("/{storyId}/like")
    public ResponseEntity<Boolean> toggleLike(
        @AuthenticationPrincipal() CustomUserDetails customUserDetails,
        @PathVariable Long storyId
    ) {
        boolean res = storyLikeService.toggleLike(customUserDetails.getUser(), storyId);
        return ResponseEntity.ok(res);
    }

    @GetMapping("/{storyId}/like/status")
    public ResponseEntity<Boolean> getLikeStatus(
        @AuthenticationPrincipal() CustomUserDetails customUserDetails,
        @PathVariable Long storyId
    ) {
        boolean res = storyLikeService.isStoryLikedByUser(customUserDetails.getUser(), storyId);
        return ResponseEntity.ok(res);
    }
}