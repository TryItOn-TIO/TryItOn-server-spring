package com.tryiton.core.story.repository;

import com.tryiton.core.story.entity.Story;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface StoryRepository extends JpaRepository<Story, Long> {
    Optional<Story> findById(Long id);

    // 최초 조회 또는 최신순 정렬
    List<Story> findAllByOrderByIdDesc(Pageable pageable);

    // 무한 스크롤을 위해 currentStoryId 보다 작은 ID를 가진 스토리들을 ID 내림차순으로 조회
    List<Story> findByIdLessThanOrderByIdDesc(Long currentStoryId, Pageable pageable);

    // 좋아요 수(인기순) 정렬을 위한 메서드 (ID 내림차순은 2차 정렬 기준)
    @Query("SELECT s FROM Story s WHERE s.likeCount < :currentLikeCount OR (s.likeCount = :currentLikeCount AND s.id < :currentStoryId) ORDER BY s.likeCount DESC, s.id DESC")
    List<Story> findPopularStoriesLessThan(Long currentStoryId, int currentLikeCount, Pageable pageable);

    // 좋아요 수(인기순)로 최초/일반 조회
    List<Story> findAllByOrderByLikeCountDescIdDesc(Pageable pageable);
}
