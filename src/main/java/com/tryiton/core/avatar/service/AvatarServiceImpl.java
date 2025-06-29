package com.tryiton.core.avatar.service;

import com.tryiton.core.avatar.dto.AvatarProductInfoDto;
import com.tryiton.core.avatar.entity.Avatar;
import com.tryiton.core.avatar.repository.AvatarItemRepository;
import com.tryiton.core.avatar.repository.AvatarRepository;
import com.tryiton.core.member.entity.Member;
import com.tryiton.core.member.repository.MemberRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AvatarServiceImpl implements AvatarService {

    private final AvatarRepository avatarRepository;
    private final AvatarItemRepository avatarItemRepository;
    private final MemberRepository memberRepository;

    // 가장 최근 착장한 아바타 이미지 + 착용 상품명 리스트
    @Override
    public AvatarProductInfoDto getLatestAvatarWithProducts(Long userId) {
        Avatar avatar = avatarRepository.findTopByUserIdOrderByCreatedAtDesc(userId);

        List<String> productNames = getProductNamesOfAvatar(avatar);

        return new AvatarProductInfoDto(avatar.getAvatarImg(), productNames);
    }

    // 북마크된 아바타들 최대 10개 + 각 착장 상품 정보 포함
    @Override
    public List<AvatarProductInfoDto> getBookmarkedAvatarsWithProducts(Long userId) {
        Member user = memberRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("해당 유저가 존재하지 않습니다."));

        return avatarRepository.findAllByUserAndIsBookmarkedTrueOrderByCreatedAtDesc(user).stream()
            .limit(10)
            .map(avatar -> new AvatarProductInfoDto(
                avatar.getAvatarImg(),
                getProductNamesOfAvatar(avatar)
            ))
            .collect(Collectors.toList());
    }

    // 공통 로직: 해당 아바타가 입은 상품명 리스트 추출
    private List<String> getProductNamesOfAvatar(Avatar avatar) {
        return avatarItemRepository.findAllByAvatar(avatar).stream()
            .map(item -> item.getProduct().getProductName())
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateBookmark(Long avatarId, Long userId, boolean bookmark) {
        Member user = memberRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("해당 유저가 존재하지 않습니다."));

        Avatar avatar = avatarRepository.findByIdAndUser(avatarId, user)
            .orElseThrow(() -> new IllegalArgumentException("해당 아바타가 존재하지 않습니다."));

        avatar.setBookmarked(bookmark);
    }
}
