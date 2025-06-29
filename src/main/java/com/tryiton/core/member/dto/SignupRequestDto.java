package com.tryiton.core.member.dto;

import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter @Setter
@SuperBuilder
@NoArgsConstructor
// Google, Email DTO로 확장
public class SignupRequestDto {
    private String username;
    private LocalDate birthDate;
    private String gender;
    private String phoneNum;

    private String preferredStyle;
    private Integer height;
    private Integer weight;
    private Integer shoeSize;

    private String avatarBaseImageUrl;
    private String userBaseImageUrl;
}
