package com.tryiton.core.mypage.dto;

import com.tryiton.core.common.enums.AuthProvider;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserProfileResponse {
    private final String email;
    private final String username;
    private final Integer height;
    private final Integer weight;
    private final Integer shoeSize;
    private final AuthProvider loginType;
}
