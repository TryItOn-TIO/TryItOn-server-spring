package com.tryiton.core.story.dto;

import com.tryiton.core.product.dto.ProductResponseDto;
import com.tryiton.core.story.entity.Author;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StoryResponseDto {

    private Long storyId;
    private String storyImageUrl;
    private String contents;
    private int likeCount;
    private boolean liked;
    private LocalDateTime createdAt;

    // AvatarItem 내의 정보
    private List<ProductResponseDto> products;

    // 작성자 정보
    private Author author;

    // 댓글 정보
    private List<CommentResponseDto> comments;
}