package com.tryiton.core.auth.oauth.dto;

import com.tryiton.core.member.dto.SignupRequestDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor
public class GoogleSignupRequestDto extends SignupRequestDto {

    private String idToken;
}