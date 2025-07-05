package com.tryiton.core.avatar.service;

import com.tryiton.core.avatar.dto.AvatarProductInfoDto;
import com.tryiton.core.avatar.dto.request.TryonAvatarTogetherNodeRequest;
import com.tryiton.core.avatar.dto.response.TryonAvatarTogetherNodeResponse;
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

    AvatarTryOnResponse tryOn(Member member, AvatarTryOnRequest avatarTryOnRequest);

    AvatarCreateResponse create(Member member, AvatarCreateRequest avatarCreateRequest);

    TryonAvatarTogetherNodeResponse tryonTogether(Member member, TryonAvatarTogetherNodeRequest tryonAvatarTogetherNodeRequest);
}
