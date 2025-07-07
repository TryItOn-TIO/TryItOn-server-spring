package com.tryiton.core.closet.dto;

import java.util.List;
import lombok.Getter;

@Getter
public class ClosetAvatarSaveRequestDto {

    private String avatarImage;
    private List<ClosetAvatarItemRequestDto> items;
}
