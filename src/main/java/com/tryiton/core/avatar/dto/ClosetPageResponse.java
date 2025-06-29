package com.tryiton.core.avatar.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ClosetPageResponse {

    private AvatarProductInfoDto latestAvatar;
    private List<AvatarProductInfoDto> bookmarkedAvatars;
}
