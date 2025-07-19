package com.tryiton.core.story.repository; // 새로운 패키지

import com.tryiton.core.member.entity.Member;
import com.tryiton.core.story.entity.Story;
import com.tryiton.core.story.entity.StoryLike;
import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StoryLikeRepository extends JpaRepository<StoryLike, Long> {
    Optional<StoryLike> findByMemberAndStory(Member member, Story story);
    boolean existsByStoryIdAndMemberId(Long storyId, Long memberId);

    @Query("SELECT sl.story.id FROM StoryLike sl WHERE sl.member.id = :memberId AND sl.story.id IN :storyIds")
    Set<Long> findStoryIdsByMemberIdAndStoryIdsIn(@Param("memberId") Long memberId, @Param("storyIds") List<Long> storyIds);
}