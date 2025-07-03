package com.tryiton.core.story.dto;

import com.tryiton.core.story.entity.Position;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CommentResponseDto {

    private Long id;
    private String username;
    private String contents;
    private LocalDateTime createdAt;

    private Position position;
}
