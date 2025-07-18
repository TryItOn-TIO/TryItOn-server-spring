package com.tryiton.core.member.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 회원 탈퇴 요청 DTO
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WithdrawRequest {
    
    // 비밀번호 (이메일 로그인 사용자 확인용)
    private String password;
    
    // 탈퇴 사유 (선택 사항)
    private String reason;
}
