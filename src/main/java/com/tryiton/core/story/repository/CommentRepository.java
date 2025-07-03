package com.tryiton.core.story.repository;

import com.tryiton.core.story.entity.Comment;
import com.tryiton.core.story.entity.Story;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    Optional<Comment> findById(Long commentId);
    List<Comment> findByStory(Story story);
    Optional<Comment> findByIdAndStory(Long commentId, Story story);
}
