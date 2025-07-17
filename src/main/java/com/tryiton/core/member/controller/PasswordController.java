package com.tryiton.core.member.controller;

import com.tryiton.core.member.dto.request.PasswordChangeRequest;
import com.tryiton.core.member.dto.response.PasswordChangeResponse;
import com.tryiton.core.member.entity.Member;
import com.tryiton.core.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/member/password")
@RequiredArgsConstructor
@Tag(name = "Password", description = "비밀번호 관리 API")
public class PasswordController {

    private final MemberService memberService;

    @Operation(summary = "비밀번호 변경 가능 여부 확인", description = "현재 사용자가 비밀번호를 변경할 수 있는지 확인합니다.")
    @GetMapping("/changeable")
    public ResponseEntity<Boolean> canChangePassword(@AuthenticationPrincipal Member member) {
        boolean canChange = memberService.canChangePassword(member.getId());
        return ResponseEntity.ok(canChange);
    }

    @Operation(summary = "비밀번호 변경", description = "사용자의 비밀번호를 변경합니다.")
    @PutMapping("/change")
    public ResponseEntity<PasswordChangeResponse> changePassword(
            @AuthenticationPrincipal Member member,
            @Valid @RequestBody PasswordChangeRequest request) {
        
        PasswordChangeResponse response = memberService.changePassword(member.getId(), request);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}
