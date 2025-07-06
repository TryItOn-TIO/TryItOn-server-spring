package com.tryiton.core.closet.dto;

import com.tryiton.core.closet.entity.ClosetAvatar;
import com.tryiton.core.closet.entity.ClosetAvatarItem;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;

@Getter
public class ClosetAvatarResponseDto {

    private final Long avatarId;
    private final String avatarImage;
    private final Map<String, ClosetAvatarItemResponseDto> itemsByCategory;

    public ClosetAvatarResponseDto(ClosetAvatar closetAvatar) {
        this.avatarId = closetAvatar.getId();
        this.avatarImage = closetAvatar.getAvatarImage();
        this.itemsByCategory = new HashMap<>();

        for (ClosetAvatarItem item : closetAvatar.getItems()) {
            // NPE 방지: parentCategory가 null인 경우 현재 카테고리 이름 사용
            String categoryName = item.getProduct().getCategory().getParentCategory() != null 
                ? item.getProduct().getCategory().getParentCategory().getCategoryName()
                : item.getProduct().getCategory().getCategoryName();
            
            itemsByCategory.put(categoryName, new ClosetAvatarItemResponseDto(item.getProduct()));
        }
    }
}
