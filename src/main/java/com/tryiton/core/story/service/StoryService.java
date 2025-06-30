package com.tryiton.core.story.service;

import com.tryiton.core.story.repository.StoryRepository;
import org.springframework.stereotype.Service;

@Service
public class StoryService {

    private final StoryRepository storyRepository;

    public StoryService(StoryRepository storyRepository) {
        this.storyRepository = storyRepository;
    }
}
