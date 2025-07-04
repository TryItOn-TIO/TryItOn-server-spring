package com.tryiton.core.mypage.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ProfileUpdateRequestDto {
    private String username;
    private Integer height;
    private Integer weight;
    private Integer shoeSize;
}
