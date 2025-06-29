package com.tryiton.core.member.dto;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SignupRequestDto {
    private String email;
    private String password;
    private String username;
    private LocalDate birthDate;
    private String gender;
    private String phoneNum;
    private String profileImageUrl;

    private String preferredStyle;
    private Integer height;
    private Integer weight;
    private Integer shoeSize;
}
