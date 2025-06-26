package com.tryiton.core.member.controller;

import com.tryiton.core.member.dto.SigninRequestDto;
import com.tryiton.core.member.dto.SigninResponseDto;
import com.tryiton.core.member.dto.SignupRequestDto;
import com.tryiton.core.member.dto.SignupResponseDto;
import com.tryiton.core.member.service.MemberService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class MemberController {
    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @PostMapping("/signup")
    public SignupResponseDto signup(@RequestBody SignupRequestDto dto){
        return memberService.signup(dto);
    }

    @PostMapping("/signin")
    public SigninResponseDto signin(@RequestBody SigninRequestDto dto){
        return memberService.signin(dto);
    }
}
