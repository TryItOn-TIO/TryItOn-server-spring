package com.tryiton.core.avatar.service;

import com.tryiton.core.avatar.dto.AvatarProductInfoDto;
import java.util.List;
import com.tryiton.core.avatar.dto.request.AvatarCreateRequest;
import com.tryiton.core.avatar.dto.request.AvatarTryOnRequest;
import com.tryiton.core.avatar.dto.response.AvatarCreateResponse;
import com.tryiton.core.avatar.dto.response.AvatarTryOnResponse;
import com.tryiton.core.member.entity.Member;
import org.springframework.stereotype.Service;

@Service
public interface AvatarService {
    AvatarProductInfoDto getLatestAvatarWithProducts(Long userId);

    List<AvatarProductInfoDto> getBookmarkedAvatarsWithProducts(Long userId);

    void updateBookmark(Long avatarId, Long userId, boolean bookmark);

    AvatarTryOnResponse tryOn(Member member, AvatarTryOnRequest avatarTryOnRequest);

    AvatarCreateResponse create(Member member, AvatarCreateRequest avatarCreateRequest);
}
