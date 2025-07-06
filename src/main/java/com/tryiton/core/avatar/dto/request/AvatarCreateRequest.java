package com.tryiton.core.avatar.dto.request;

import com.tryiton.core.avatar.entity.Avatar;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.checkerframework.checker.units.qual.N;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AvatarCreateRequest {

    private String userId;
    private String tryOnImgUrl;


}
