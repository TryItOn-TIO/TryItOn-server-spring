package com.tryiton.core.closet.service;

import com.tryiton.core.avatar.entity.Avatar;
import com.tryiton.core.avatar.repository.AvatarRepository;
import com.tryiton.core.closet.dto.ClosetResponse;
import com.tryiton.core.closet.entity.Closet;
import com.tryiton.core.closet.repository.ClosetRepository;
import com.tryiton.core.common.exception.BusinessException;
import com.tryiton.core.member.entity.Member;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClosetServiceImpl implements ClosetService {

    private final ClosetRepository closetRepository;
    private final AvatarRepository avatarRepository;

    @Override
    @Transactional
    public void addToCloset(Member member, Long avatarId) {
        closetRepository.findByMemberAndAvatarId(member, avatarId).ifPresent(closet -> {
            throw new BusinessException(HttpStatus.CONFLICT, "이미 옷장에 추가된 아바타입니다.");
        });

        Avatar avatar = avatarRepository.findById(avatarId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "아바타를 찾을 수 없습니다."));

        Closet closet = Closet.builder()
                .member(member)
                .avatar(avatar)
                .build();

        closetRepository.save(closet);
    }

    @Override
    @Transactional
    public void removeFromCloset(Member member, Long closetId) {
        Closet closet = closetRepository.findById(closetId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "옷장에서 해당 아이템을 찾을 수 없습니다."));

        if (!closet.getMember().equals(member)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "삭제 권한이 없습니다.");
        }

        closetRepository.delete(closet);
    }

    @Override
    public List<ClosetResponse> getClosetItems(Member member) {
        return closetRepository.findAllByMemberOrderByCreatedAtDesc(member).stream()
                .map(closet -> ClosetResponse.of(closet, getProductNamesOfAvatar(closet.getAvatar())))
                .collect(Collectors.toList());
    }

    private List<String> getProductNamesOfAvatar(Avatar avatar) {
        return avatar.getItems().stream()
                .map(item -> item.getProduct().getProductName())
                .collect(Collectors.toList());
    }
}
