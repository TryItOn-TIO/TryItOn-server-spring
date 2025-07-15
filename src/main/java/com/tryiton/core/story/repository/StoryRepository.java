package com.tryiton.core.story.repository;

import com.tryiton.core.story.entity.Story;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface StoryRepository extends JpaRepository<Story, Long> {
    Optional<Story> findById(Long id);

    // 단일 스토리 조회 시 author와 profile을 함께 조회
    @Query("SELECT s FROM Story s LEFT JOIN FETCH s.author a LEFT JOIN FETCH a.profile WHERE s.id = :id")
    Optional<Story> findByIdWithAuthor(Long id);
    
    // N+1 쿼리 해결: 단일 스토리 조회 시 연관 엔티티를 함께 조회 (comments는 별도 조회)
    @Query("SELECT DISTINCT s FROM Story s " +
           "LEFT JOIN FETCH s.author a " +
           "LEFT JOIN FETCH a.profile " +
           "LEFT JOIN FETCH s.closetAvatar ca " +
           "LEFT JOIN FETCH ca.items cai " +
           "LEFT JOIN FETCH cai.product " +
           "WHERE s.id = :id")
    Optional<Story> findByIdWithAllAssociations(Long id);

    // 최초 조회 또는 최신순 정렬 (author와 profile을 함께 조회)
    @Query("SELECT s FROM Story s LEFT JOIN FETCH s.author a LEFT JOIN FETCH a.profile ORDER BY s.id DESC")
    List<Story> findAllByOrderByIdDesc(Pageable pageable);
    
    // N+1 쿼리 해결: 최신순 정렬 시 연관 엔티티를 함께 조회 (comments는 별도 조회)
    @Query("SELECT DISTINCT s FROM Story s " +
           "LEFT JOIN FETCH s.author a " +
           "LEFT JOIN FETCH a.profile " +
           "LEFT JOIN FETCH s.closetAvatar ca " +
           "LEFT JOIN FETCH ca.items cai " +
           "LEFT JOIN FETCH cai.product " +
           "ORDER BY s.id DESC")
    List<Story> findAllByOrderByIdDescWithAllAssociations(Pageable pageable);

    // 무한 스크롤을 위해 currentStoryId 보다 작은 ID를 가진 스토리들을 ID 내림차순으로 조회 (author와 profile을 함께 조회)
    @Query("SELECT s FROM Story s LEFT JOIN FETCH s.author a LEFT JOIN FETCH a.profile WHERE s.id < :currentStoryId ORDER BY s.id DESC")
    List<Story> findByIdLessThanOrderByIdDesc(Long currentStoryId, Pageable pageable);
    
    // N+1 쿼리 해결: 무한 스크롤 시 연관 엔티티를 함께 조회 (comments는 별도 조회)
    @Query("SELECT DISTINCT s FROM Story s " +
           "LEFT JOIN FETCH s.author a " +
           "LEFT JOIN FETCH a.profile " +
           "LEFT JOIN FETCH s.closetAvatar ca " +
           "LEFT JOIN FETCH ca.items cai " +
           "LEFT JOIN FETCH cai.product " +
           "WHERE s.id < :currentStoryId ORDER BY s.id DESC")
    List<Story> findByIdLessThanOrderByIdDescWithAllAssociations(Long currentStoryId, Pageable pageable);

    // 좋아요 수(인기순) 정렬을 위한 메서드 (ID 내림차순은 2차 정렬 기준, author와 profile을 함께 조회)
    @Query("SELECT s FROM Story s LEFT JOIN FETCH s.author a LEFT JOIN FETCH a.profile WHERE s.likeCount < :currentLikeCount OR (s.likeCount = :currentLikeCount AND s.id < :currentStoryId) ORDER BY s.likeCount DESC, s.id DESC")
    List<Story> findPopularStoriesLessThan(Long currentStoryId, int currentLikeCount, Pageable pageable);
    
    // N+1 쿼리 해결: 인기순 정렬 시 연관 엔티티를 함께 조회 (comments는 별도 조회)
    @Query("SELECT DISTINCT s FROM Story s " +
           "LEFT JOIN FETCH s.author a " +
           "LEFT JOIN FETCH a.profile " +
           "LEFT JOIN FETCH s.closetAvatar ca " +
           "LEFT JOIN FETCH ca.items cai " +
           "LEFT JOIN FETCH cai.product " +
           "WHERE s.likeCount < :currentLikeCount OR (s.likeCount = :currentLikeCount AND s.id < :currentStoryId) " +
           "ORDER BY s.likeCount DESC, s.id DESC")
    List<Story> findPopularStoriesLessThanWithAllAssociations(Long currentStoryId, int currentLikeCount, Pageable pageable);

    // 좋아요 수(인기순)로 최초/일반 조회 (author와 profile을 함께 조회)
    @Query("SELECT s FROM Story s LEFT JOIN FETCH s.author a LEFT JOIN FETCH a.profile ORDER BY s.likeCount DESC, s.id DESC")
    List<Story> findAllByOrderByLikeCountDescIdDesc(Pageable pageable);
    
    // N+1 쿼리 해결: 인기순 최초 조회 시 연관 엔티티를 함께 조회 (comments는 별도 조회)
    @Query("SELECT DISTINCT s FROM Story s " +
           "LEFT JOIN FETCH s.author a " +
           "LEFT JOIN FETCH a.profile " +
           "LEFT JOIN FETCH s.closetAvatar ca " +
           "LEFT JOIN FETCH ca.items cai " +
           "LEFT JOIN FETCH cai.product " +
           "ORDER BY s.likeCount DESC, s.id DESC")
    List<Story> findAllByOrderByLikeCountDescIdDescWithAllAssociations(Pageable pageable);
}
