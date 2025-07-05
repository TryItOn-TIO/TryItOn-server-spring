package com.tryiton.core.closet.dto;

import com.tryiton.core.avatar.dto.AvatarProductInfoDto;
import com.tryiton.core.product.dto.ProductResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClosetResponseDto {
    
    // 현재 착장 (최신 아바타)
    private AvatarProductInfoDto currentOutfit;
    
    // 저장된 착장들 (북마크된 아바타들)
    private List<AvatarProductInfoDto> savedOutfits;
    
    // 찜 목록
    private List<ProductResponseDto> wishlistProducts;
    
    // 찜 목록 총 개수
    private int wishlistCount;
}
