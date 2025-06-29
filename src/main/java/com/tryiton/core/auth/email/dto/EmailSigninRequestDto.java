package com.tryiton.core.auth.email.dto;

import com.tryiton.core.member.dto.SigninRequestDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter @Setter
@SuperBuilder
@NoArgsConstructor
public class EmailSigninRequestDto extends SigninRequestDto {

}
