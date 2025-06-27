package com.tryiton.core.auth.email.dto;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailSignupRequestDto {

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
