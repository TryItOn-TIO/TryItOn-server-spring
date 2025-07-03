package com.tryiton.core.story.dto;

import com.tryiton.core.story.entity.Position;
import lombok.Getter;

@Getter
public class CommentRequestDto {

    private String contents;
    private Position position;
}
