package com.tryiton.core.closet.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class ClosetAddRequest {

    @NotNull(message = "아바타 ID는 필수입니다.")
    private Long avatarId;

}
