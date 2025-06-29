package com.tryiton.core.avatar.dto;

import java.util.List;

public class AvatarSaveRequest {

    private String avatarImg; // 완성된 착장 이미지 (S3 URL)
    private List<Long> productId; // 착장된 상품 ID 목록
}
