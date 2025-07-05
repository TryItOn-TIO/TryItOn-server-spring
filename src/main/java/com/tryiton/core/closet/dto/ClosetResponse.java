package com.tryiton.core.closet.dto;

import com.tryiton.core.avatar.dto.AvatarProductInfoDto;
import com.tryiton.core.closet.entity.Closet;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClosetResponse {

    private Long closetId;
    private AvatarProductInfoDto avatarInfo;

    public static ClosetResponse of(Closet closet, List<String> productNames) {
        return ClosetResponse.builder()
                .closetId(closet.getId())
                .avatarInfo(new AvatarProductInfoDto(closet.getAvatar().getAvatarImg(), productNames))
                .build();
    }

}
