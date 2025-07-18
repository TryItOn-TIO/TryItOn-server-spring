package com.tryiton.core.member.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 회원 탈퇴 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WithdrawResponse {
    
    private boolean success;
    private String message;
    
    /**
     * 회원 탈퇴 성공 응답 생성
     * @return 성공 응답
     */
    public static WithdrawResponse success() {
        return WithdrawResponse.builder()
                .success(true)
                .message("회원 탈퇴가 완료되었습니다.")
                .build();
    }
    
    /**
     * 회원 탈퇴 실패 응답 생성
     * @param message 실패 메시지
     * @return 실패 응답
     */
    public static WithdrawResponse failure(String message) {
        return WithdrawResponse.builder()
                .success(false)
                .message(message)
                .build();
    }
}
