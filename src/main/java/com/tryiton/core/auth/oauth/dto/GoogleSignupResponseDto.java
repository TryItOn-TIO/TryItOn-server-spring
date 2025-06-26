package com.tryiton.core.auth.oauth.dto;

import com.tryiton.core.member.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GoogleSignupResponseDto {

    private String email;
    private String username;
    private String accessToken;

    public static GoogleSignupResponseDto from(Member member, String accessToken) {
        return new GoogleSignupResponseDto(
            member.getEmail(),
            member.getUsername(),
            accessToken
        );
    }
}
