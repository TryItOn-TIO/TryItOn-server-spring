package com.tryiton.core.member.dto.response;

import lombok.Getter;

@Getter
public class PasswordChangeResponse {
    
    private final boolean success;
    private final String message;

    private PasswordChangeResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public static PasswordChangeResponse success() {
        return new PasswordChangeResponse(true, "비밀번호가 성공적으로 변경되었습니다.");
    }

    public static PasswordChangeResponse failure(String message) {
        return new PasswordChangeResponse(false, message);
    }
}
