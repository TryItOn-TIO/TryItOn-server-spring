package com.tryiton.core.story.controller;

import com.tryiton.core.auth.security.CustomUserDetails;
import com.tryiton.core.common.enums.StorySort;
import com.tryiton.core.story.dto.StoriesResponseDto;
import com.tryiton.core.story.dto.StoryPutDto;
import com.tryiton.core.story.dto.StoryRequestDto;
import com.tryiton.core.story.dto.StoryResponseDto;
import com.tryiton.core.story.service.StoryService;
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
public class StoryController {

    private final StoryService storyService;

    public StoryController(StoryService storyService) {
        this.storyService = storyService;
    }

    @PostMapping
    public ResponseEntity<Boolean> createStory(
        @AuthenticationPrincipal() CustomUserDetails customUserDetails,
        StoryRequestDto storyRequestDto
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
        StoriesResponseDto storiesResponseDto = storyService.getStories(customUserDetails.getUser(), sort, limit);
        return ResponseEntity.ok(storiesResponseDto);
    }

    @GetMapping("/next")
    public ResponseEntity<StoriesResponseDto> getNextStories(
        @AuthenticationPrincipal() CustomUserDetails customUserDetails,
        @RequestParam Long currentStoryId,
        @RequestParam StorySort sort,
        @RequestParam Integer limit
    ){
        StoriesResponseDto storiesResponseDto = storyService.getNextStories(customUserDetails.getUser(), currentStoryId, sort, limit);
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
}
