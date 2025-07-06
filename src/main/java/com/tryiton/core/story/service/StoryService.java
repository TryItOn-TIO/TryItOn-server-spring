package com.tryiton.core.story.service;

import com.tryiton.core.closet.entity.ClosetAvatar;
import com.tryiton.core.closet.repository.ClosetAvatarRepository;
import com.tryiton.core.common.enums.StorySort;
import com.tryiton.core.common.exception.BusinessException;
import com.tryiton.core.member.entity.Member;
import com.tryiton.core.product.dto.ProductResponseDto;
import com.tryiton.core.story.dto.CommentResponseDto;
import com.tryiton.core.story.dto.StoriesResponseDto;
import com.tryiton.core.story.dto.StoryPutDto;
import com.tryiton.core.story.dto.StoryRequestDto;
import com.tryiton.core.story.dto.StoryResponseDto;
import com.tryiton.core.story.entity.Author;
import com.tryiton.core.story.entity.Story;
import com.tryiton.core.story.repository.StoryLikeRepository;
import com.tryiton.core.story.repository.StoryRepository;
import com.tryiton.core.wishlist.repository.WishlistRepository;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class StoryService {

    private final StoryRepository storyRepository;
    private final ClosetAvatarRepository closetAvatarRepository;
    private final WishlistRepository wishlistRepository;
    private final StoryLikeRepository storyLikeRepository;

    public StoryService(StoryRepository storyRepository, ClosetAvatarRepository closetAvatarRepository,
        WishlistRepository wishlistRepository, StoryLikeRepository storyLikeRepository) {
        this.storyRepository = storyRepository;
        this.closetAvatarRepository = closetAvatarRepository;
        this.wishlistRepository = wishlistRepository;
        this.storyLikeRepository = storyLikeRepository;
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
        Story story = storyRepository.findByIdWithAuthor(storyId)
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
        Story story = storyRepository.findByIdWithAuthor(storyId)
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
        List<Story> stories = Collections.emptyList();

        switch (sort) {
            case LATEST:
                stories = storyRepository.findAllByOrderByIdDesc(pageable);
                break;
            case POPULAR:
                stories = storyRepository.findAllByOrderByLikeCountDescIdDesc(pageable);
                break;
            default:
                stories = storyRepository.findAllByOrderByIdDesc(pageable);
                break;
        }

        return mapToStoriesResponseDto(stories, user.getId());
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
        List<Story> stories = Collections.emptyList();

        switch (sort) {
            case LATEST:
                stories = storyRepository.findByIdLessThanOrderByIdDesc(currentStoryId, pageable);
                break;
            case POPULAR:
                Story currentStory = storyRepository.findByIdWithAuthor(currentStoryId)
                    .orElseThrow(() -> new NoSuchElementException("ID가 " + currentStoryId + "인 스토리를 찾을 수 없습니다."));

                stories = storyRepository.findPopularStoriesLessThan(currentStoryId, currentStory.getLikeCount(), pageable);
                break;
            default:
                stories = storyRepository.findByIdLessThanOrderByIdDesc(currentStoryId, pageable);
                break;
        }

        return mapToStoriesResponseDto(stories, user.getId());
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

    private StoryResponseDto mapToStoryResponseDto(Story story, Long currentUserId){
        // Author 매핑
        Author author = null;
        if (story.getAuthor() != null) {
            author = Author.builder()
                .id(story.getAuthor().getId())
                .username(story.getAuthor().getUsername())
                .profileImageUrl(
                    story.getAuthor().getProfile() != null ?
                        story.getAuthor().getProfile().getProfileImageUrl() : null
                )
                .build();
        }

        // currentUserId를 사용하여 해당 스토리의 좋아요 여부 확인
        boolean isStoryLiked = false;
        if (currentUserId != null && storyLikeRepository != null ) {
            isStoryLiked = storyLikeRepository.existsByStoryIdAndMemberId(story.getId(), currentUserId);
        }

        // Products 매핑
        List<ProductResponseDto> productResponseDtos = Collections.emptyList();
        if (story.getClosetAvatar() != null && story.getClosetAvatar().getItems() != null) {
            productResponseDtos = story.getClosetAvatar().getItems().stream()
                .filter(avatarItem -> avatarItem.getProduct() != null)
                .map(avatarItem -> {
                    // currentUserId를 사용하여 해당 상품의 찜 여부 확인
                    boolean isProductLiked = false;
                    if (currentUserId != null) {
                        // wishlistRepository에 Member ID와 Product ID로 찜 여부를 확인하는 메서드가 필요
                        isProductLiked = wishlistRepository.existsByUserIdAndProductId(currentUserId, avatarItem.getProduct().getId());
                    }
                    return new ProductResponseDto(avatarItem.getProduct(), isProductLiked);
                })
                .collect(Collectors.toList());
        }

        // Comments 매핑
        List<CommentResponseDto> comments = Collections.emptyList();
        if (story.getComments() != null) {
            comments = story.getComments().stream()
                .map(comment -> {
                    // CommentResponseDto.username은 comment.getAuthor().getUsername()에서 가져와야 함.
                    // Position은 @Embeddable이므로 직접 사용 가능.
                    return CommentResponseDto.builder()
                        .id(comment.getId())
                        .username(comment.getAuthor() != null ? comment.getAuthor().getUsername() : null) // comment.getAuthor()의 null 체크
                        .contents(comment.getContents())
                        .position(comment.getPosition())
                        .createdAt(comment.getCreatedAt())
                        .build();
                })
                .collect(Collectors.toList());
        }

        return StoryResponseDto.builder()
            .storyId(story.getId())
            .storyImageUrl(story.getStoryImageUrl())
            .contents(story.getContents())
            .likeCount(story.getLikeCount())
            .liked(isStoryLiked)
            .createdAt(story.getCreatedAt())
            .products(productResponseDtos)
            .author(author)
            .comments(comments)
            .build();
    }

    private StoriesResponseDto mapToStoriesResponseDto(List<Story> stories, Long currentUserId) {
        List<StoryResponseDto> storyResponseDtos = stories.stream()
            .map(story -> {

                // Author 매핑
                Author author = null;
                if (story.getAuthor() != null) {
                    author = Author.builder()
                        .id(story.getAuthor().getId())
                        .username(story.getAuthor().getUsername())
                        .profileImageUrl(
                            story.getAuthor().getProfile() != null ?
                                story.getAuthor().getProfile().getProfileImageUrl() : null
                        )
                        .build();
                }

                // Products 매핑
                List<ProductResponseDto> productResponseDtos = Collections.emptyList();
                if (story.getClosetAvatar() != null && story.getClosetAvatar().getItems() != null) {
                    productResponseDtos = story.getClosetAvatar().getItems().stream()
                        .filter(avatarItem -> avatarItem.getProduct() != null)
                        .map(avatarItem -> {
                            // currentUserId를 사용하여 해당 상품의 찜 여부 확인
                            boolean isProductLiked = false;
                            if (currentUserId != null) {
                                isProductLiked = wishlistRepository.existsByUserIdAndProductId(currentUserId, avatarItem.getProduct().getId());
                            }
                            return new ProductResponseDto(avatarItem.getProduct(), isProductLiked);
                        })
                        .collect(Collectors.toList());
                }

                // Comments 매핑
                List<CommentResponseDto> comments = Collections.emptyList();
                if (story.getComments() != null) {
                    comments = story.getComments().stream()
                        .map(comment -> {
                            return CommentResponseDto.builder()
                                .id(comment.getId())
                                .username(comment.getAuthor() != null ? comment.getAuthor().getUsername() : null) // comment.getAuthor()의 null 체크
                                .contents(comment.getContents())
                                .position(comment.getPosition())
                                .createdAt(comment.getCreatedAt())
                                .build();
                        })
                        .collect(Collectors.toList());
                }

                // currentUserId를 사용하여 해당 스토리의 좋아요 여부 확인
                boolean isStoryLiked = false;
                if (currentUserId != null && storyLikeRepository != null ) {
                     isStoryLiked = storyLikeRepository.existsByStoryIdAndMemberId(story.getId(), currentUserId);
                }

                return StoryResponseDto.builder()
                    .storyId(story.getId())
                    .storyImageUrl(story.getStoryImageUrl())
                    .contents(story.getContents())
                    .likeCount(story.getLikeCount())
                    .liked(isStoryLiked)
                    .createdAt(story.getCreatedAt())
                    .products(productResponseDtos)
                    .author(author)
                    .comments(comments)
                    .build();
            })
            .collect(Collectors.toList());

        return StoriesResponseDto.builder()
            .stories(storyResponseDtos)
            .length(storyResponseDtos.size())
            .build();
    }
}
