package com.tryiton.core.avatar.service;

import com.tryiton.core.avatar.dto.AvatarProductInfoDto;
import java.util.List;

public interface AvatarService {

    AvatarProductInfoDto getLatestAvatarWithProducts(Long userId);

    List<AvatarProductInfoDto> getBookmarkedAvatarsWithProducts(Long userId);

    void updateBookmark(Long avatarId, Long userId, boolean bookmark);
}
