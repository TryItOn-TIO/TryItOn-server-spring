package com.tryiton.core.story.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StorySummaryDto {
    private Long storyId;
    private String storyImageUrl;
    private String contents;
    private int likeCount;
    private boolean liked;
    private LocalDateTime createdAt;
    private AuthorDto author;
    private long productCount; // long 타입이 더 안전합니다 (COUNT 쿼리 결과)
    private long commentCount; // long 타입이 더 안전합니다 (COUNT 쿼리 결과)

    // JPA 프로젝션을 위한 생성자
    public StorySummaryDto(Long storyId, String storyImageUrl, String contents, int likeCount, LocalDateTime createdAt, Long authorId, String authorUsername, String authorProfileImageUrl, long productCount, long commentCount) {
        this.storyId = storyId;
        this.storyImageUrl = storyImageUrl;
        this.contents = contents;
        this.likeCount = likeCount;
        this.createdAt = createdAt;
        this.author = new AuthorDto(authorId, authorUsername, authorProfileImageUrl);
        this.productCount = productCount;
        this.commentCount = commentCount;
        // 'liked' 필드는 서비스 레이어에서 별도로 설정해야 합니다.
    }
}
