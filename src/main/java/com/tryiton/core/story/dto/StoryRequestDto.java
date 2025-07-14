package com.tryiton.core.story.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StoryRequestDto {
    private Long avatarId;
    private String contents;
    private String storyImageUrl;
}
