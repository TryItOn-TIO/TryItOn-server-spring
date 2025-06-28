package com.tryiton.core.avatar.service;

import com.tryiton.core.avatar.entity.Avatar;
import com.tryiton.core.avatar.repository.AvatarRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AvatarService {

    private final AvatarRepository avatarRepository;

    public String getLatestTryOnImage(Long userId) {
        Avatar avatar = avatarRepository.findTopByUserIdOrderByCreatedAtDesc(userId);
        return avatar.getTryOnImg();
    }
}
