package com.tryiton.core.story.controller;

import com.tryiton.core.auth.security.CustomUserDetails;
import com.tryiton.core.story.dto.CommentRequestDto;
import com.tryiton.core.story.dto.CommentResponseDto;
import com.tryiton.core.story.service.CommentService;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/story")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping("/{storyId}/comments")
    public ResponseEntity<CommentResponseDto> createComment(
        @AuthenticationPrincipal() CustomUserDetails customUserDetails,
        @PathVariable Long storyId,
        @RequestBody CommentRequestDto requestDto
    ) {
        CommentResponseDto commentResponseDto = commentService.createComment(customUserDetails.getUser(), storyId, requestDto);
        return ResponseEntity.ok(commentResponseDto);
    }

    @GetMapping("/{storyId}/comments")
    public ResponseEntity<List<CommentResponseDto>> getComments(@PathVariable Long storyId) {
        List<CommentResponseDto> commentResponseDtos = commentService.getCommentsByStoryId(storyId);
        return ResponseEntity.ok(commentResponseDtos);
    }

    @PutMapping("/{storyId}/comments/{commentId}")
    public ResponseEntity<CommentResponseDto> updateComment(
        @AuthenticationPrincipal() CustomUserDetails customUserDetails,
        @PathVariable Long storyId,
        @PathVariable Long commentId,
        @RequestBody CommentRequestDto requestDto
    ) {
        CommentResponseDto commentResponseDto = commentService.updateComment(customUserDetails.getUser(), storyId, commentId, requestDto);
        return ResponseEntity.ok(commentResponseDto);
    }

    @DeleteMapping("/{storyId}/comments/{commentId}")
    public ResponseEntity<Boolean> deleteComment(
        @AuthenticationPrincipal() CustomUserDetails customUserDetails,
        @PathVariable Long storyId,
        @PathVariable Long commentId
    ) {
        boolean res = commentService.deleteComment(customUserDetails.getUser(), storyId, commentId);
        return ResponseEntity.ok(res);
    }
}
