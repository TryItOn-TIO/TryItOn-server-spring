package com.tryiton.core.story.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class StoriesResponseDto {
    private List<StoryResponseDto> stories;
    private int length;

}