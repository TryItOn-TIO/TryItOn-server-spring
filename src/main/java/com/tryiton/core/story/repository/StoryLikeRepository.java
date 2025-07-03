package com.tryiton.core.story.repository; // 새로운 패키지

import com.tryiton.core.member.entity.Member;
import com.tryiton.core.story.entity.Story;
import com.tryiton.core.story.entity.StoryLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StoryLikeRepository extends JpaRepository<StoryLike, Long> {
    Optional<StoryLike> findByMemberAndStory(Member member, Story story);
    boolean existsByStoryIdAndMemberId(Long storyId, Long memberId);
}