package com.tryiton.core.avatar.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class AvatarResponse {
    private String taskId;
    private String status;
}
