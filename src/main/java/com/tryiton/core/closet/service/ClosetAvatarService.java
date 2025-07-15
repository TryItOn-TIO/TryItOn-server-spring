package com.tryiton.core.closet.service;

import com.tryiton.core.avatar.entity.Avatar;
import com.tryiton.core.avatar.entity.AvatarItem;
import com.tryiton.core.avatar.repository.AvatarRepository;
import com.tryiton.core.closet.dto.ClosetAvatarResponseDto;
import com.tryiton.core.closet.entity.ClosetAvatar;
import com.tryiton.core.closet.entity.ClosetAvatarItem;
import com.tryiton.core.closet.repository.ClosetAvatarRepository;
import com.tryiton.core.common.enums.RecommendAction;
import com.tryiton.core.common.exception.BusinessException;
import com.tryiton.core.member.entity.Member;
import com.tryiton.core.product.entity.Product;
import com.tryiton.core.product.repository.ProductRepository;
import com.tryiton.core.recommend.service.RecommendBehaviorLogService;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ClosetAvatarService {

    private final ClosetAvatarRepository closetAvatarRepository;
    private final ProductRepository productRepository;
    private final AvatarRepository avatarRepository;

    private final RecommendBehaviorLogService recommendBehaviorLogService;

    // 옷장 전체 조회
    @Transactional(readOnly = true)
    public List<ClosetAvatarResponseDto> getClosetAvatarByUser(Long userId) {
        List<ClosetAvatar> avatars = closetAvatarRepository.findWithItemsByUserId(userId);
        return avatars.stream()
            .map(ClosetAvatarResponseDto::new)
            .collect(Collectors.toList());
    }

    // 착장 저장
    @Transactional
    public void saveClosetAvatar(Member user) {
        // 최대 10개 제한 확인
        validateMaxAvatarLimit(user.getId());
        
        // 새로운 아바타 생성
        ClosetAvatar newAvatar = createClosetAvatar(user);
        
        // 중복 확인
        validateDuplicateAvatar(user.getId(), newAvatar);
        
        // 저장
        closetAvatarRepository.save(newAvatar);

        // 유저 행동 로그 비동기 기록
        for (ClosetAvatarItem avatarItem: newAvatar.getItems()){
            recommendBehaviorLogService.logUserAction(user.getId(), avatarItem.getProduct().getId(),
                RecommendAction.TRYONCLOSET);
        }
    }

    // 착장 삭제
    @Transactional
    public void deleteClosetAvatar(Long avatarId, Long userId) {
        ClosetAvatar avatar = closetAvatarRepository.findByIdAndUserId(avatarId, userId)
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "해당 착장을 찾을 수 없거나 권한이 없습니다."));
        
        closetAvatarRepository.delete(avatar);
    }

    // 최대 아바타 개수 제한 검증
    private void validateMaxAvatarLimit(Long userId) {
        long count = closetAvatarRepository.countByUserId(userId);
        if (count >= 10) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "최대 10개의 착장만 저장할 수 있습니다.");
        }
    }

    // 새로운 ClosetAvatar 생성
    private ClosetAvatar createClosetAvatar(Member user) {
        Avatar avatar = avatarRepository.findTopByMemberIdOrderByCreatedAtDesc(user.getId());
        if (avatar == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "사용자의 아바타를 찾을 수 없습니다.");
        }

        ClosetAvatar closetAvatar = ClosetAvatar.builder()
            .user(user)
            .avatarImage(avatar.getAvatarImg())
            .build();

        List<AvatarItem> avatarItems = avatar.getItems();
        if (avatarItems == null || avatarItems.isEmpty()){
            throw new BusinessException(HttpStatus.BAD_REQUEST, "아바타에 착용된 아이템이 없습니다.");
        }

        for (AvatarItem avatarItem : avatarItems) {
            if (avatarItem.getProduct() == null){
                continue;
            }

            Product product = productRepository.findByIdWithCategory(avatarItem.getProduct().getId())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND,
                    "상품을 찾을 수 없습니다. ID: " + avatarItem.getProduct().getId()));

            ClosetAvatarItem item = ClosetAvatarItem.builder()
                .product(product)
                .build();

            closetAvatar.addItem(item);
        }

        return closetAvatar;
    }

    // 중복 아바타 검증
    private void validateDuplicateAvatar(Long userId, ClosetAvatar newAvatar) {
        List<ClosetAvatar> existingAvatars = closetAvatarRepository.findWithItemsByUserId(userId);
        
        boolean isDuplicate = existingAvatars.stream()
            .anyMatch(existing -> existing.hasSameCombination(newAvatar.getItems()));
        
        if (isDuplicate) {
            throw new BusinessException(HttpStatus.CONFLICT, "이미 동일한 착장이 저장되어 있습니다.");
        }
    }

    // 중복 착장 존재 확인 (public 메서드로 유지 - 외부에서 사용할 수 있도록)
    @Transactional(readOnly = true)
    public boolean isDuplicateCombination(Long userId, List<ClosetAvatarItem> items) {
        List<ClosetAvatar> savedAvatars = closetAvatarRepository.findWithItemsByUserId(userId);
        return savedAvatars.stream()
            .anyMatch(avatar -> avatar.hasSameCombination(items));
    }
}
