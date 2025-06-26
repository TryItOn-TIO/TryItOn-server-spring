package com.tryiton.core.member.dto;

import com.tryiton.core.member.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SignupResponseDto {
    private String email;
    private String username;

    public static SignupResponseDto from(Member member) {
        return new SignupResponseDto(
            member.getEmail(),
            member.getUsername()
        );
    }
}
