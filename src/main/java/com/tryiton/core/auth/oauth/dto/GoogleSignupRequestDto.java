package com.tryiton.core.auth.oauth.dto;

import java.time.LocalDate;
import lombok.Getter;

@Getter
public class GoogleSignupRequestDto {
    private String username;
    private LocalDate birthDate;
    private String gender;
    private String phoneNum;

    private String preferredStyle;
    private Integer height;  // cm
    private Integer weight;  // kg
    private Integer shoeSize;

    private String idToken;
}