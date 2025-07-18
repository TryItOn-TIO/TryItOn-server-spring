package com.tryiton.core.member.controller;

import com.tryiton.core.auth.security.SecurityUtil;
import com.tryiton.core.member.dto.request.WithdrawRequest;
import com.tryiton.core.member.dto.response.WithdrawResponse;
import com.tryiton.core.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class MemberController {
    
    private final MemberService memberService;
    
    /**
     * 회원 탈퇴 API
     * @param request 탈퇴 요청 정보
     * @return 탈퇴 처리 결과
     */
    @DeleteMapping("/withdraw")
    public ResponseEntity<WithdrawResponse> withdrawMember(@RequestBody WithdrawRequest request) {
        // SecurityUtil을 사용하여 현재 인증된 사용자의 ID를 가져옴
        Long currentUserId = SecurityUtil.getCurrentUserId();
        
        WithdrawResponse response = memberService.withdrawMember(currentUserId, request);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}
