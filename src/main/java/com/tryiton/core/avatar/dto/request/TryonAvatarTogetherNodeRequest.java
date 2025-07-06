package com.tryiton.core.avatar.dto.request;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 여러 상의와 하의 조합의 가상 피팅을 요청하기 위한 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TryonAvatarTogetherNodeRequest {

    // 가상 피팅을 시도할 상의 상품 ID 리스트 (최대 5개)
    private List<Long> topProductIds;

    // 가상 피팅을 시도할 하의 상품 ID 리스트 (최대 5개)
    private List<Long> bottomProductIds;
}