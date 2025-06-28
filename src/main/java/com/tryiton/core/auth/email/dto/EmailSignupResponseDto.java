package com.tryiton.core.auth.email.dto;

import com.tryiton.core.member.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class EmailSignupResponseDto {
    private String email;
    private String username;

    public static EmailSignupResponseDto from(Member member) {
        return EmailSignupResponseDto.builder()
            .email(member.getEmail())
            .username(member.getUsername())
            .build();
    }
}
