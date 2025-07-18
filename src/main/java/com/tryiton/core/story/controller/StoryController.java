package com.tryiton.core.story.controller;

import com.tryiton.core.auth.security.CustomUserDetails;
import com.tryiton.core.common.enums.StorySort;
import com.tryiton.core.member.entity.Member;
import com.tryiton.core.story.dto.BackgroundRemovalResponse;
import com.tryiton.core.story.dto.StoriesResponseDto;
import com.tryiton.core.story.dto.StoryPutDto;
import com.tryiton.core.story.dto.StoryRequestDto;
import com.tryiton.core.story.dto.StoryResponseDto;
import com.tryiton.core.story.service.StoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stories")
@Tag(name = "스토리", description = "스토리 관리 API")
public class StoryController {

    private final StoryService storyService;

    public StoryController(StoryService storyService) {
        this.storyService = storyService;
    }

    @PostMapping
    public ResponseEntity<Boolean> createStory(
        @AuthenticationPrincipal() CustomUserDetails customUserDetails,
        @RequestBody StoryRequestDto storyRequestDto
    ){
        boolean res = storyService.postStory(customUserDetails.getUser(), storyRequestDto);
        return ResponseEntity.ok(res);
    }

    @GetMapping
    public ResponseEntity<StoriesResponseDto> getStories(
        @AuthenticationPrincipal() CustomUserDetails customUserDetails,
        @RequestParam StorySort sort,
        @RequestParam Integer limit
    ){
        // 비로그인 사용자도 접근 가능하도록 수정
        Member user = (customUserDetails != null) ? customUserDetails.getUser() : null;
        StoriesResponseDto storiesResponseDto = storyService.getStories(user, sort, limit);
        return ResponseEntity.ok(storiesResponseDto);
    }

    @GetMapping("/next")
    public ResponseEntity<StoriesResponseDto> getNextStories(
        @AuthenticationPrincipal() CustomUserDetails customUserDetails,
        @RequestParam Long currentStoryId,
        @RequestParam StorySort sort,
        @RequestParam Integer limit
    ){
        // 비로그인 사용자도 접근 가능하도록 수정
        Member user = (customUserDetails != null) ? customUserDetails.getUser() : null;
        StoriesResponseDto storiesResponseDto = storyService.getNextStories(user, currentStoryId, sort, limit);
        return ResponseEntity.ok(storiesResponseDto);
    }

    @GetMapping("/my")
    public ResponseEntity<List<StoryResponseDto>> getMyStories(
        @AuthenticationPrincipal() CustomUserDetails customUserDetails
    ){
        List<StoryResponseDto> storiesResponseDto = storyService.getMyStories(customUserDetails.getUser());
        return ResponseEntity.ok(storiesResponseDto);
    }

    @PutMapping("/{storyId}")
    public ResponseEntity<StoryResponseDto> updateStory(
        @AuthenticationPrincipal() CustomUserDetails customUserDetails,
        @PathVariable Long storyId,
        @RequestBody StoryPutDto storyPutDto
    ){
        StoryResponseDto storyResponseDto = storyService.updateStory(customUserDetails.getUser(), storyId, storyPutDto);
        return ResponseEntity.ok(storyResponseDto);
    }

    @DeleteMapping("/{storyId}")
    public ResponseEntity<Boolean> deleteComment(
        @AuthenticationPrincipal() CustomUserDetails customUserDetails,
        @PathVariable Long storyId
    ){
        boolean res = storyService.deleteStory(customUserDetails.getUser(), storyId);
        return ResponseEntity.ok(res);
    }

    @Operation(
        summary = "이미지 배경 제거",
        description = "스토리용 이미지의 배경을 제거합니다 (누끼 따기)"
    )
    @PostMapping("/remove-background")
    public ResponseEntity<BackgroundRemovalResponse> removeBackground(
        @AuthenticationPrincipal() CustomUserDetails customUserDetails,
        @RequestParam String imageUrl
    ) {
        Member user = customUserDetails.getUser();
        BackgroundRemovalResponse response = storyService.removeBackground(user, imageUrl);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @Operation(
        summary = "누끼 딴 스토리 작성",
        description = "이미지 배경을 자동으로 제거한 후 스토리를 작성합니다"
    )
    @PostMapping("/with-background-removal")
    public ResponseEntity<Boolean> createStoryWithBackgroundRemoval(
        @AuthenticationPrincipal() CustomUserDetails customUserDetails,
        @RequestBody StoryRequestDto storyRequestDto
    ) {
        boolean res = storyService.postStoryWithBackgroundRemoval(customUserDetails.getUser(), storyRequestDto);
        return ResponseEntity.ok(res);
    }
}
