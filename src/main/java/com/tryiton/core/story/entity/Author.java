package com.tryiton.core.story.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Embeddable
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Author {

    private Long id;
    private String username;
    private String profileImageUrl;
}
