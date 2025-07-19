package com.tryiton.core.story.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
public class StorySummaryDto {
    private Long storyId;
    private String storyImageUrl;
    private String contents;
    private int likeCount;
    @Setter
    private boolean liked;
    private LocalDateTime createdAt;
    private AuthorDto author;
    private long productCount;
    private long commentCount;

    // JPA 프로젝션을 위한 생성자
    public StorySummaryDto(Long storyId, String storyImageUrl, String contents, int likeCount, LocalDateTime createdAt,
                           Long authorId, String authorUsername, String authorProfileImageUrl,
                           long productCount, long commentCount) {
        this.storyId = storyId;
        this.storyImageUrl = storyImageUrl;
        this.contents = contents;
        this.likeCount = likeCount;
        this.createdAt = createdAt;
        this.author = new AuthorDto(authorId, authorUsername, authorProfileImageUrl);
        this.productCount = productCount;
        this.commentCount = commentCount;
        this.liked = false; // 기본값, 서비스 레이어에서 설정 필요
    }
}
