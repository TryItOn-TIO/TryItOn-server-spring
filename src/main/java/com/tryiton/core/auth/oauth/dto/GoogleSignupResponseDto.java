package com.tryiton.core.auth.oauth.dto;

import com.tryiton.core.member.dto.SignupResponseDto;
import com.tryiton.core.member.entity.Member;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor
public class GoogleSignupResponseDto extends SignupResponseDto {

    private String accessToken;

    public static GoogleSignupResponseDto from(Member member, String accessToken) {
        return GoogleSignupResponseDto.builder()
            .email(member.getEmail())
            .username(member.getUsername())
            .accessToken(accessToken)
            .build();
    }
}
