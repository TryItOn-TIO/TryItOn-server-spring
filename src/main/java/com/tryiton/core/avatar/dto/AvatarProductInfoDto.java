package com.tryiton.core.avatar.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

// 옷장, 메인화면, 카테고리 탭에서 아바타 이미지 + 착용 상품을 보여줄 때 사용
// 서버가 클라이언트에게 데이터를 내려줄 때 사용하는 DTO (Response 용)
@Getter
@AllArgsConstructor
public class AvatarProductInfoDto {

    private String avatarImg;
    private List<String> productNames; // 아바타가 입고 있는 상품들의 이름 목록
}
