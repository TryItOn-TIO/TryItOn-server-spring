package com.tryiton.core.mypage.dto;

import com.tryiton.core.member.entity.Member;
import com.tryiton.core.member.entity.Profile;
import lombok.Getter;

@Getter
public class ProfileResponseDto {
    private final String email;
    private final String username;
    private final Integer height;
    private final Integer weight;
    private final Integer shoeSize;

    public ProfileResponseDto(Member member, Profile profile) {
        this.email = member.getEmail();
        this.username = member.getUsername();
        this.height = profile != null ? profile.getHeight() : null;
        this.weight = profile != null ? profile.getWeight() : null;
        this.shoeSize = profile != null ? profile.getShoeSize() : null;
    }
}
