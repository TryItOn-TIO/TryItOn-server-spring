package com.tryiton.core.story.service;

import com.tryiton.core.common.exception.BusinessException;
import com.tryiton.core.member.entity.Member;
import com.tryiton.core.story.dto.CommentRequestDto;
import com.tryiton.core.story.dto.CommentResponseDto;
import com.tryiton.core.story.entity.Comment;
import com.tryiton.core.story.entity.Story;
import com.tryiton.core.story.repository.CommentRepository;
import com.tryiton.core.story.repository.StoryRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final StoryRepository storyRepository;

    public CommentService(CommentRepository commentRepository, StoryRepository storyRepository) {
        this.commentRepository = commentRepository;
        this.storyRepository = storyRepository;
    }

    @Transactional
    public CommentResponseDto createComment(Member author, Long storyId, CommentRequestDto requestDto) {
        Story story = storyRepository.findById(storyId)
            .orElseThrow(() -> new IllegalArgumentException("스토리를 찾을 수 없습니다." + storyId));

        Comment newComment = Comment.builder()
            .story(story)
            .author(author)
            .contents(requestDto.getContents())
            .position(requestDto.getPosition())
            .build();

        commentRepository.save(newComment);
        return mapToCommentResponseDto(newComment);
    }

    public List<CommentResponseDto> getCommentsByStoryId(Long storyId) {
        Story story = storyRepository.findById(storyId)
            .orElseThrow(() -> new IllegalArgumentException("스토리를 찾을 수 없습니다." + storyId));

        List<Comment> comments = commentRepository.findByStory(story);

        return comments.stream()
            .map(this::mapToCommentResponseDto)
            .collect(Collectors.toList());
    }

    @Transactional
    public CommentResponseDto updateComment(Member author, Long storyId, Long commentId, CommentRequestDto requestDto) {
        Story story = storyRepository.findById(storyId)
            .orElseThrow(() -> new IllegalArgumentException("스토리를 찾을 수 없습니다." + storyId));
        Comment comment = commentRepository.findByIdAndStory(commentId, story)
            .orElseThrow(() -> new IllegalArgumentException("해당 스토리의 댓글을 찾을 수 없습니다." + commentId));

        // 댓글 작성자와 수정 요청자가 일치하는지 확인
        if (!comment.getAuthor().getId().equals(author.getId())) {
            throw new BusinessException("댓글을 수정할 권한이 없습니다.");
        }

        comment.update(requestDto.getContents(), requestDto.getPosition());
        commentRepository.save(comment);

        return mapToCommentResponseDto(comment);
    }

    @Transactional
    public boolean deleteComment(Member author, Long storyId, Long commentId) {
        Story story = storyRepository.findById(storyId)
            .orElseThrow(() -> new IllegalArgumentException("스토리를 찾을 수 없습니다." + storyId));
        Comment comment = commentRepository.findByIdAndStory(commentId, story)
            .orElseThrow(() -> new IllegalArgumentException("해당 스토리의 댓글을 찾을 수 없습니다." + commentId));

        // 권한 확인
        boolean isCommentAuthor = comment.getAuthor().getId().equals(author.getId());
        boolean isStoryAuthor = story.getAuthor().getId().equals(author.getId());

        if (!isCommentAuthor && !isStoryAuthor) {
            throw new BusinessException("댓글을 삭제할 권한이 없습니다.");
        }

        commentRepository.delete(comment);
        return true;
    }

    private CommentResponseDto mapToCommentResponseDto(Comment comment) {
        return CommentResponseDto.builder()
            .id(comment.getId())
            .username(comment.getAuthor() != null ? comment.getAuthor().getUsername() : null)
            .contents(comment.getContents())
            .createdAt(comment.getCreatedAt())
            .position(comment.getPosition())
            .build();
    }
}
