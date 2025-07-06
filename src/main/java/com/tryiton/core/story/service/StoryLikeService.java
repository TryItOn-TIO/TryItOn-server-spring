package com.tryiton.core.story.service;

import com.tryiton.core.member.entity.Member;
import com.tryiton.core.story.entity.Story;
import com.tryiton.core.story.entity.StoryLike;
import com.tryiton.core.story.repository.StoryRepository;
import com.tryiton.core.story.repository.StoryLikeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StoryLikeService {

    private final StoryLikeRepository storyLikeRepository;
    private final StoryRepository storyRepository;

    public StoryLikeService(StoryLikeRepository storyLikeRepository,
        StoryRepository storyRepository) {
        this.storyLikeRepository = storyLikeRepository;
        this.storyRepository = storyRepository;
    }

    @Transactional
    public boolean toggleLike(Member member, Long storyId) {
        Story story = storyRepository.findById(storyId)
            .orElseThrow(() -> new IllegalArgumentException("스토리를 찾을 수 없습니다: " + storyId));

        // 해당 사용자가 이 스토리에 좋아요를 이미 눌렀는지 확인
        return storyLikeRepository.findByMemberAndStory(member, story)
            .map(storyLike -> {
                // 이미 좋아요가 있으면 좋아요 취소 (삭제)
                storyLikeRepository.delete(storyLike);
                story.decrementLikeCount(); // 스토리의 좋아요 수 감소
                return false; // 좋아요 취소됨
            })
            .orElseGet(() -> {
                // 좋아요가 없으면 새로 추가
                StoryLike newLike = StoryLike.builder()
                    .member(member)
                    .story(story)
                    .build();
                storyLikeRepository.save(newLike);
                story.incrementLikeCount(); // 스토리의 좋아요 수 증가
                return true; // 좋아요 추가됨
            });
    }

    @Transactional(readOnly = true)
    public boolean isStoryLikedByUser(Member member, Long storyId) {
        return storyLikeRepository.existsByStoryIdAndMemberId(storyId, member.getId());
    }
}