package com.tryiton.core.story.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class StoriesSummaryResponseDto {
    private List<StorySummaryDto> stories;
    private int length;
}
