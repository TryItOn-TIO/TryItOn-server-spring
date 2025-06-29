package com.tryiton.core.auth.email.dto;

import com.tryiton.core.member.dto.SignupRequestDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter @Setter
@SuperBuilder
@NoArgsConstructor
public class EmailSignupRequestDto extends SignupRequestDto {

    private String email;
    private String password;
    private String profileImageUrl;
}
