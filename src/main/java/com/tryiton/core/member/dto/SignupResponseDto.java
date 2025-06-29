package com.tryiton.core.member.dto;

import com.tryiton.core.member.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class SignupResponseDto {
    private String email;
    private String username;

    public static SignupResponseDto from(Member member) {
        return SignupResponseDto.builder()
            .email(member.getEmail())
            .username(member.getUsername())
            .build();
    }
}
