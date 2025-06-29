package com.tryiton.core.auth.oauth.dto;

import com.tryiton.core.member.dto.SigninResponseDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter @Setter
@SuperBuilder
@NoArgsConstructor
public class GoogleSigninResponseDto extends SigninResponseDto {

}
