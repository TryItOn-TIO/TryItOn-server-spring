package com.tryiton.core.story.repository;

import com.tryiton.core.story.dto.StorySummaryDto;
import com.tryiton.core.story.entity.Story;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StoryRepository extends JpaRepository<Story, Long> {

    @Query("SELECT new com.tryiton.core.story.dto.StorySummaryDto(" +
            "s.id, s.storyImageUrl, s.contents, s.likeCount, s.createdAt, " +
            "a.id, a.username, p.profileImageUrl, " +
            "SIZE(ca.items), SIZE(s.comments)) " +
            "FROM Story s " +
            "LEFT JOIN s.author a " +
            "LEFT JOIN a.profile p " +
            "LEFT JOIN s.closetAvatar ca " +
            "ORDER BY s.id DESC")
    List<StorySummaryDto> findStorySummaries(Pageable pageable);

    @Query("SELECT new com.tryiton.core.story.dto.StorySummaryDto(" +
            "s.id, s.storyImageUrl, s.contents, s.likeCount, s.createdAt, " +
            "a.id, a.username, p.profileImageUrl, " +
            "SIZE(ca.items), SIZE(s.comments)) " +
            "FROM Story s " +
            "LEFT JOIN s.author a " +
            "LEFT JOIN a.profile p " +
            "LEFT JOIN s.closetAvatar ca " +
            "WHERE s.id < :storyId " +
            "ORDER BY s.id DESC")
    List<StorySummaryDto> findNextStorySummaries(@Param("storyId") Long storyId, Pageable pageable);

    @Query("SELECT new com.tryiton.core.story.dto.StorySummaryDto(" +
            "s.id, s.storyImageUrl, s.contents, s.likeCount, s.createdAt, " +
            "a.id, a.username, p.profileImageUrl, " +
            "SIZE(ca.items), SIZE(s.comments)) " +
            "FROM Story s " +
            "LEFT JOIN s.author a " +
            "LEFT JOIN a.profile p " +
            "LEFT JOIN s.closetAvatar ca " +
            "ORDER BY s.likeCount DESC, s.id DESC")
    List<StorySummaryDto> findPopularStorySummaries(Pageable pageable);

    @Query("SELECT new com.tryiton.core.story.dto.StorySummaryDto(" +
            "s.id, s.storyImageUrl, s.contents, s.likeCount, s.createdAt, " +
            "a.id, a.username, p.profileImageUrl, " +
            "SIZE(ca.items), SIZE(s.comments)) " +
            "FROM Story s " +
            "LEFT JOIN s.author a " +
            "LEFT JOIN a.profile p " +
            "LEFT JOIN s.closetAvatar ca " +
            "WHERE s.likeCount < :likeCount OR (s.likeCount = :likeCount AND s.id < :storyId) " +
            "ORDER BY s.likeCount DESC, s.id DESC")
    List<StorySummaryDto> findNextPopularStorySummaries(@Param("storyId") Long storyId, @Param("likeCount") int likeCount, Pageable pageable);

    @Query("SELECT s.id FROM Story s ORDER BY s.id DESC")
    List<Long> findStoryIdsByOrderByIdDesc(Pageable pageable);


    @Query("SELECT s.id FROM Story s WHERE s.id < :currentStoryId ORDER BY s.id DESC")
    List<Long> findStoryIdsByIdLessThanOrderByIdDesc(Long currentStoryId, Pageable pageable);

    @Query("SELECT s.id FROM Story s WHERE s.likeCount < :currentLikeCount OR (s.likeCount = :currentLikeCount AND s.id < :currentStoryId) ORDER BY s.likeCount DESC, s.id DESC")
    List<Long> findPopularStoryIdsLessThan(Long currentStoryId, int currentLikeCount, Pageable pageable);

    @Query("SELECT s.id FROM Story s ORDER BY s.likeCount DESC, s.id DESC")
    List<Long> findStoryIdsByOrderByLikeCountDescIdDesc(Pageable pageable);

    @Query("SELECT DISTINCT s FROM Story s " +
            "LEFT JOIN FETCH s.author a " +
            "LEFT JOIN FETCH a.profile " +
            "LEFT JOIN FETCH s.closetAvatar ca " +
            "LEFT JOIN FETCH ca.items cai " +
            "LEFT JOIN FETCH cai.product p " +
            "LEFT JOIN FETCH p.category " +
            "WHERE s.id IN :storyIds " +
            "ORDER BY s.likeCount DESC, s.id DESC")
    List<Story> findAllByIdInWithAllAssociationsOrderByLikeCount(List<Long> storyIds);

    @Query("SELECT DISTINCT s FROM Story s " +
            "LEFT JOIN FETCH s.author a " +
            "LEFT JOIN FETCH a.profile " +
            "LEFT JOIN FETCH s.closetAvatar ca " +
            "LEFT JOIN FETCH ca.items cai " +
            "LEFT JOIN FETCH cai.product p " +
            "LEFT JOIN FETCH p.category " +
            "WHERE s.id IN :storyIds " +
            "ORDER BY s.id DESC")
    List<Story> findAllByIdInWithAllAssociationsOrderById(List<Long> storyIds);

    @Query("SELECT DISTINCT s FROM Story s " +
            "LEFT JOIN FETCH s.author a " +
            "LEFT JOIN FETCH a.profile " +
            "LEFT JOIN FETCH s.closetAvatar ca " +
            "LEFT JOIN FETCH ca.items cai " +
            "LEFT JOIN FETCH cai.product p " +
            "LEFT JOIN FETCH p.category " +
            "WHERE s.id = :id")
    Optional<Story> findByIdWithAllAssociations(Long id);

    @Query("SELECT DISTINCT s FROM Story s " +
            "LEFT JOIN FETCH s.author a " +
            "LEFT JOIN FETCH a.profile " +
            "LEFT JOIN FETCH s.closetAvatar ca " +
            "LEFT JOIN FETCH ca.items cai " +
            "LEFT JOIN FETCH cai.product p " +
            "LEFT JOIN FETCH p.category " +
            "WHERE s.author.id = :userId " +
            "ORDER BY s.id DESC")
    List<Story> findByAuthorIdWithAllAssociations(Long userId);
}