package com.tryiton.core.story.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class BackgroundRemovalResponse {
    private String originalImageUrl;     // 원본 이미지 URL
    private String processedImageUrl;    // 누끼 딴 이미지 URL
    private boolean success;
    private String message;
    
    public static BackgroundRemovalResponse success(String originalUrl, String processedUrl) {
        return BackgroundRemovalResponse.builder()
                .originalImageUrl(originalUrl)
                .processedImageUrl(processedUrl)
                .success(true)
                .message("배경 제거가 성공적으로 완료되었습니다.")
                .build();
    }
    
    public static BackgroundRemovalResponse failure(String message) {
        return BackgroundRemovalResponse.builder()
                .success(false)
                .message(message)
                .build();
    }
}
