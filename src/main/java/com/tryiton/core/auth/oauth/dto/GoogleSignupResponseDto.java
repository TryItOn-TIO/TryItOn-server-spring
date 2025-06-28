package com.tryiton.core.auth.oauth.dto;

import com.tryiton.core.member.entity.Member;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GoogleSignupResponseDto {

    private String email;
    private String username;
    private String accessToken;

    public static GoogleSignupResponseDto from(Member member, String accessToken) {
        return GoogleSignupResponseDto.builder()
            .email(member.getEmail())
            .username(member.getUsername())
            .accessToken(accessToken)
            .build();
    }
}
