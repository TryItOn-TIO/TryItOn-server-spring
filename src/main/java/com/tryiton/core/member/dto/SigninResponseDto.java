package com.tryiton.core.member.dto;

import com.tryiton.core.member.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SigninResponseDto {
    private String nickname;
    private String email;

    public static SigninResponseDto from(Member member) {
        return new SigninResponseDto(
            member.getNickname(),
            member.getEmail()
        );
    }
}
