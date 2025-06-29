package com.tryiton.core.auth.email.dto;

import com.tryiton.core.member.dto.SignupResponseDto;
import com.tryiton.core.member.entity.Member;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor
public class EmailSignupResponseDto extends SignupResponseDto {

    public static EmailSignupResponseDto from(Member saved) {
        return EmailSignupResponseDto.builder()
            .email(saved.getEmail())
            .username(saved.getUsername())
            .build();
    }
}
