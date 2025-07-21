package com.tryiton.core.story.service;

import com.tryiton.core.closet.entity.ClosetAvatar;
import com.tryiton.core.closet.repository.ClosetAvatarRepository;
import com.tryiton.core.common.enums.StorySort;
import com.tryiton.core.common.exception.BusinessException;
import com.tryiton.core.member.entity.Member;
import com.tryiton.core.product.dto.ProductResponseDto;
import com.tryiton.core.story.dto.AuthorDto;
import com.tryiton.core.story.dto.BackgroundRemovalRequest;
import com.tryiton.core.story.dto.BackgroundRemovalResponse;
import com.tryiton.core.story.dto.CommentResponseDto;
import com.tryiton.core.story.dto.StoriesResponseDto;
import com.tryiton.core.story.dto.StoryPutDto;
import com.tryiton.core.story.dto.StoryRequestDto;
import com.tryiton.core.story.dto.StoryResponseDto;
import com.tryiton.core.story.entity.Story;
import com.tryiton.core.story.repository.StoryLikeRepository;
import com.tryiton.core.story.repository.StoryRepository;
import com.tryiton.core.story.repository.CommentRepository;
import com.tryiton.core.story.dto.StoriesSummaryResponseDto;
import com.tryiton.core.story.dto.StorySummaryDto;
import com.tryiton.core.wishlist.repository.WishlistRepository;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Service
@Transactional(readOnly = true)
public class StoryService {

    private final StoryRepository storyRepository;
    private final ClosetAvatarRepository closetAvatarRepository;
    private final WishlistRepository wishlistRepository;
    private final StoryLikeRepository storyLikeRepository;
    private final CommentRepository commentRepository;
    private final WebClient userServiceWebClient;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    @Value("${cloud.aws.region.static}")
    private String region;

    public StoryService(StoryRepository storyRepository, ClosetAvatarRepository closetAvatarRepository,
        WishlistRepository wishlistRepository, StoryLikeRepository storyLikeRepository,
        CommentRepository commentRepository, WebClient userServiceWebClient) {
        this.storyRepository = storyRepository;
        this.closetAvatarRepository = closetAvatarRepository;
        this.wishlistRepository = wishlistRepository;
        this.storyLikeRepository = storyLikeRepository;
        this.commentRepository = commentRepository;
        this.userServiceWebClient = userServiceWebClient;
    }

    @Transactional
    public boolean postStory(Member author, StoryRequestDto storyRequestDto){
        // avatarId null 체크
        if (storyRequestDto.getAvatarId() == null) {
            throw new IllegalArgumentException("아바타 ID가 필요합니다.");
        }
        
         ClosetAvatar closetAvatar = closetAvatarRepository.findById(storyRequestDto.getAvatarId())
             .orElseThrow(() -> new IllegalArgumentException("옷장에서 해당 아바타를 찾을 수 없습니다."));

        Story newStory = Story.builder()
            .author(author)
            .closetAvatar(closetAvatar)
            .storyImageUrl(storyRequestDto.getStoryImageUrl())
            .contents(storyRequestDto.getContents())
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .likeCount(0)
            .build();

        storyRepository.save(newStory);
        return true;
    }

    @Transactional
    public StoryResponseDto updateStory(Member author, Long storyId, StoryPutDto storyPutDto){
        // N+1 쿼리 해결: 모든 연관 엔티티를 함께 조회
        Story story = storyRepository.findByIdWithAllAssociations(storyId)
            .orElseThrow(() -> new IllegalArgumentException("해당 스토리를 찾을 수 없습니다."));

        // 권한 확인
        if (!story.getAuthor().getId().equals(author.getId())){
            throw new BusinessException(HttpStatus.FORBIDDEN, "스토리를 수정할 권한이 없습니다.");
        }

        story.update(storyPutDto.getContents(), LocalDateTime.now());
        storyRepository.save(story);

        return mapToStoryResponseDto(story, author.getId());
    }

    @Transactional
    public boolean deleteStory(Member author, Long storyId){
        // N+1 쿼리 해결: 모든 연관 엔티티를 함께 조회
        Story story = storyRepository.findByIdWithAllAssociations(storyId)
            .orElseThrow(() -> new IllegalArgumentException("해당 스토리를 찾을 수 없습니다."));

        // 권한 확인
        boolean isStoryAuthor = story.getAuthor().getId().equals(author.getId());

        if (!isStoryAuthor){
            throw new BusinessException(HttpStatus.FORBIDDEN, "스토리를 삭제할 권한이 없습니다.");
        }

        storyRepository.delete(story);
        return true;
    }

    /**
     * 최초 10개의 스토리를 반환합니다. 정렬 기준을 적용할 수 있습니다.
     *
     * @param sort  스토리를 정렬할 기준 (LATEST, POPULAR)
     * @param limit 반환할 스토리의 개수
     * @return StoriesResponseDto
     */
    public StoriesResponseDto getStories(Member user, StorySort sort, Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 10;
        }

        PageRequest pageable = createPageRequest(0, limit, sort);
        List<Long> storyIds = Collections.emptyList();

        switch (sort) {
            case LATEST:
                storyIds = storyRepository.findStoryIdsByOrderByIdDesc(pageable);
                break;
            case POPULAR:
                storyIds = storyRepository.findStoryIdsByOrderByLikeCountDescIdDesc(pageable);
                break;
            default:
                storyIds = storyRepository.findStoryIdsByOrderByIdDesc(pageable);
                break;
        }

        if (storyIds.isEmpty()) {
            return StoriesResponseDto.builder().stories(Collections.emptyList()).length(0).build();
        }

        List<Story> stories;
        if (sort == StorySort.POPULAR) {
            stories = storyRepository.findAllByIdInWithAllAssociationsOrderByLikeCount(storyIds);
        } else {
            stories = storyRepository.findAllByIdInWithAllAssociationsOrderById(storyIds);
        }

        if (user == null){
            return mapToStoriesResponseDto(stories, null);
        } else{
            return mapToStoriesResponseDto(stories, user.getId());
        }
    }

    /**
     * 특정 currentStoryId를 기준으로 다음 10개의 스토리를 반환합니다.
     * 인기순 정렬의 경우, currentLikeCount도 필요합니다.
     *
     * @param currentStoryId   현재 페이지의 마지막 스토리 ID
     * @param sort             스토리를 정렬할 기준
     * @param limit            반환할 스토리의 개수 (기본 10)
     * @return StoriesResponseDto
     */
    public StoriesResponseDto getNextStories(Member user, Long currentStoryId, StorySort sort, Integer limit) {
        if (currentStoryId == null) {
            return getStories(user, sort, limit); // currentStoryId가 없으면 최초 호출과 동일하게 처리
        }

        if (limit == null || limit <= 0) {
            limit = 10;
        }

        PageRequest pageable = createPageRequest(0, limit, sort);
        List<Long> storyIds = Collections.emptyList();

        switch (sort) {
            case LATEST:
                storyIds = storyRepository.findStoryIdsByIdLessThanOrderByIdDesc(currentStoryId, pageable);
                break;
            case POPULAR:
                Story currentStory = storyRepository.findById(currentStoryId)
                    .orElseThrow(() -> new NoSuchElementException("ID가 " + currentStoryId + "인 스토리를 찾을 수 없습니다."));
                storyIds = storyRepository.findPopularStoryIdsLessThan(currentStoryId, currentStory.getLikeCount(), pageable);
                break;
            default:
                storyIds = storyRepository.findStoryIdsByIdLessThanOrderByIdDesc(currentStoryId, pageable);
                break;
        }

        if (storyIds.isEmpty()) {
            return StoriesResponseDto.builder().stories(Collections.emptyList()).length(0).build();
        }

        List<Story> stories;
        if (sort == StorySort.POPULAR) {
            stories = storyRepository.findAllByIdInWithAllAssociationsOrderByLikeCount(storyIds);
        } else {
            stories = storyRepository.findAllByIdInWithAllAssociationsOrderById(storyIds);
        }

        if (user == null){
            return mapToStoriesResponseDto(stories, null);
        } else{
            return mapToStoriesResponseDto(stories, user.getId());
        }
    }

    public StoriesSummaryResponseDto getStoriesSummary(Member user, StorySort sort, Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 10;
        }

        PageRequest pageable = createPageRequest(0, limit, sort);
        List<StorySummaryDto> summaries;

        if (sort == StorySort.POPULAR) {
            summaries = storyRepository.findPopularStorySummaries(pageable);
        } else {
            summaries = storyRepository.findStorySummaries(pageable);
        }

        if (summaries.isEmpty()) {
            return StoriesSummaryResponseDto.builder().stories(Collections.emptyList()).length(0).build();
        }

        updateLikedStatus(user, summaries);
        return StoriesSummaryResponseDto.builder()
                .stories(summaries)
                .length(summaries.size())
                .build();
    }

    public StoriesSummaryResponseDto getNextStoriesSummary(Member user, Long currentStoryId, StorySort sort, Integer limit) {
        if (currentStoryId == null) {
            return getStoriesSummary(user, sort, limit);
        }

        if (limit == null || limit <= 0) {
            limit = 10;
        }

        PageRequest pageable = createPageRequest(0, limit, sort);
        List<StorySummaryDto> summaries;

        if (sort == StorySort.POPULAR) {
            Story currentStory = storyRepository.findById(currentStoryId)
                    .orElseThrow(() -> new NoSuchElementException("ID가 " + currentStoryId + "인 스토리를 찾을 수 없습니다."));
            summaries = storyRepository.findNextPopularStorySummaries(currentStoryId, currentStory.getLikeCount(), pageable);
        } else {
            summaries = storyRepository.findNextStorySummaries(currentStoryId, pageable);
        }

        if (summaries.isEmpty()) {
            return StoriesSummaryResponseDto.builder().stories(Collections.emptyList()).length(0).build();
        }

        updateLikedStatus(user, summaries);
        return StoriesSummaryResponseDto.builder()
                .stories(summaries)
                .length(summaries.size())
                .build();
    }

    private void updateLikedStatus(Member user, List<StorySummaryDto> summaries) {
        if (user != null) {
            List<Long> storyIds = summaries.stream().map(StorySummaryDto::getStoryId).collect(Collectors.toList());
            Set<Long> likedStoryIds = storyLikeRepository.findStoryIdsByMemberIdAndStoryIdsIn(user.getId(), storyIds);
            summaries.forEach(summary -> summary.setLiked(likedStoryIds.contains(summary.getStoryId())));
        }
    }

    public List<StoryResponseDto> getMyStories(Member user) {
        // N+1 쿼리 해결: 모든 연관 엔티티를 함께 조회
        List<Story> stories = storyRepository.findByAuthorIdWithAllAssociations(user.getId());
        
        return stories.stream()
            .map(story -> mapToStoryResponseDto(story, user.getId()))
            .collect(Collectors.toList());
    }

    /**
     * PageRequest 객체를 생성하여 정렬 기준을 적용합니다.
     *
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지당 개수
     * @param sort 정렬 기준 Enum
     * @return PageRequest 객체
     */
    private PageRequest createPageRequest(int page, int size, StorySort sort) {
        Sort.Direction direction = Sort.Direction.DESC;

        if (sort == StorySort.POPULAR) {
            return PageRequest.of(page, size, Sort.by(direction, "likeCount", "id"));
        } else {
            return PageRequest.of(page, size, Sort.by(direction, "id"));
        }
    }

    private StoryResponseDto mapToStoryResponseDto(Story story, Long currentUserId) {
        return mapToStoriesResponseDto(Collections.singletonList(story), currentUserId).getStories().get(0);
    }

    private StoriesResponseDto mapToStoriesResponseDto(List<Story> stories, Long currentUserId) {
        if (stories.isEmpty()) {
            return StoriesResponseDto.builder().stories(Collections.emptyList()).length(0).build();
        }

        List<Long> storyIds = stories.stream().map(Story::getId).collect(Collectors.toList());

        Set<Long> likedStoryIds = (currentUserId != null) ?
                storyLikeRepository.findStoryIdsByMemberIdAndStoryIdsIn(currentUserId, storyIds) :
                Collections.emptySet();

        List<Long> productIds = stories.stream()
                .flatMap(s -> s.getClosetAvatar().getItems().stream())
                .map(item -> item.getProduct().getId())
                .collect(Collectors.toList());

        Set<Long> wishlistedProductIds = (currentUserId != null && !productIds.isEmpty()) ?
                new java.util.HashSet<>(wishlistRepository.findProductIdsByUserIdAndProductIds(currentUserId, productIds)) :
                Collections.emptySet();

        Map<Long, List<CommentResponseDto>> commentsMap = commentRepository.findByStoryIdIn(storyIds).stream()
                .collect(Collectors.groupingBy(
                        comment -> comment.getStory().getId(),
                        Collectors.mapping(
                                comment -> CommentResponseDto.builder()
                                        .id(comment.getId())
                                        .username(comment.getAuthor() != null ? comment.getAuthor().getUsername() : null)
                                        .contents(comment.getContents())
                                        .position(comment.getPosition())
                                        .createdAt(comment.getCreatedAt())
                                        .build(),
                                Collectors.toList()
                        )
                ));

        List<StoryResponseDto> storyResponseDtos = stories.stream()
                .map(story -> {
                    AuthorDto author = story.getAuthor() != null ? AuthorDto.builder()
                            .id(story.getAuthor().getId())
                            .username(story.getAuthor().getUsername())
                            .profileImageUrl(story.getAuthor().getProfile() != null ? story.getAuthor().getProfile().getProfileImageUrl() : null)
                            .build() : null;

                    List<ProductResponseDto> productResponseDtos = story.getClosetAvatar() != null && story.getClosetAvatar().getItems() != null ?
                            story.getClosetAvatar().getItems().stream()
                                    .filter(avatarItem -> avatarItem.getProduct() != null)
                                    .map(avatarItem -> new ProductResponseDto(avatarItem.getProduct(), wishlistedProductIds.contains(avatarItem.getProduct().getId())))
                                    .collect(Collectors.toList()) : Collections.emptyList();

                    return StoryResponseDto.builder()
                            .storyId(story.getId())
                            .storyImageUrl(story.getStoryImageUrl())
                            .contents(story.getContents())
                            .likeCount(story.getLikeCount())
                            .liked(likedStoryIds.contains(story.getId()))
                            .createdAt(story.getCreatedAt())
                            .products(productResponseDtos)
                            .author(author)
                            .comments(commentsMap.getOrDefault(story.getId(), Collections.emptyList()))
                            .build();
                })
                .collect(Collectors.toList());

        return StoriesResponseDto.builder()
                .stories(storyResponseDtos)
                .length(storyResponseDtos.size())
                .build();
    }

    /**
     * 스토리 이미지의 배경을 제거합니다 (누끼 따기)
     */
    public BackgroundRemovalResponse removeBackground(Member user, String imageUrl) {
        try {
            log.info("배경 제거 요청 - userId: {}, imageUrl: {}", user.getId(), imageUrl);

            // AI 서버에 배경 제거 요청
            BackgroundRemovalRequest request = new BackgroundRemovalRequest(
                imageUrl,
                user.getId(),
                null // storyId는 선택사항
            );

            // FastAPI 서버 호출
            String processedImageUrl = userServiceWebClient.post()
                .uri("/remove-background")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(String.class)
                .block();

            if (processedImageUrl == null || processedImageUrl.isBlank()) {
                throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, 
                    "배경 제거 처리 중 오류가 발생했습니다.");
            }

            log.info("배경 제거 완료 - userId: {}, processedUrl: {}", user.getId(), processedImageUrl);

            return BackgroundRemovalResponse.success(imageUrl, processedImageUrl);

        } catch (Exception e) {
            log.error("배경 제거 실패 - userId: {}, error: {}", user.getId(), e.getMessage());
            return BackgroundRemovalResponse.failure("배경 제거 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 스토리 작성 시 자동으로 배경을 제거한 이미지를 생성합니다
     */
    @Transactional
    public boolean postStoryWithBackgroundRemoval(Member author, StoryRequestDto storyRequestDto) {
        try {
            // 1. 원본 이미지의 배경 제거
            BackgroundRemovalResponse bgRemovalResponse = removeBackground(author, storyRequestDto.getStoryImageUrl());
            
            if (!bgRemovalResponse.isSuccess()) {
                log.warn("배경 제거 실패, 원본 이미지로 스토리 작성 - userId: {}", author.getId());
                // 배경 제거 실패 시 원본 이미지로 스토리 작성
                return postStory(author, storyRequestDto);
            }

            // 2. 배경 제거된 이미지로 스토리 작성
            StoryRequestDto modifiedRequest = new StoryRequestDto(
                storyRequestDto.getAvatarId(),
                storyRequestDto.getContents(),
                bgRemovalResponse.getProcessedImageUrl() // 누끼 딴 이미지 사용
            );

            return postStory(author, modifiedRequest);

        } catch (Exception e) {
            log.error("배경 제거 스토리 작성 실패 - userId: {}, error: {}", author.getId(), e.getMessage());
            // 실패 시 원본 이미지로 스토리 작성
            return postStory(author, storyRequestDto);
        }
    }
}
