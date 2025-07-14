package com.tryiton.core.story.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BackgroundRemovalRequest {
    private String originalImageUrl;  // 원본 이미지 URL
    private Long userId;              // 사용자 ID
    private Long storyId;             // 스토리 ID (선택사항)
}
