package com.tryiton.core.story.repository;

import com.tryiton.core.story.entity.Comment;
import com.tryiton.core.story.entity.Story;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    Optional<Comment> findById(Long commentId);
    List<Comment> findByStory(Story story);
    Optional<Comment> findByIdAndStory(Long commentId, Story story);

    @Query("SELECT c FROM Comment c JOIN FETCH c.author WHERE c.story.id IN :storyIds")
    List<Comment> findByStoryIdIn(@Param("storyIds") List<Long> storyIds);
}
