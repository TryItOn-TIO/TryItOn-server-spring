package com.tryiton.core.story.repository;

import com.tryiton.core.story.entity.Story;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoryRepository extends JpaRepository<Story, Long> {
    Optional<Story> findById(Long storyId);
}
