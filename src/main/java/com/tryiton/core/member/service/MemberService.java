package com.tryiton.core.member.service;

import com.tryiton.core.auth.jwt.JwtUtil;
import com.tryiton.core.common.enums.AuthProvider;
import com.tryiton.core.member.dto.SigninRequestDto;
import com.tryiton.core.member.dto.SigninResponseDto;
import com.tryiton.core.member.dto.SignupRequestDto;
import com.tryiton.core.member.dto.SignupResponseDto;
import com.tryiton.core.member.entity.Member;
import com.tryiton.core.member.entity.Profile;
import com.tryiton.core.member.repository.MemberRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final JwtUtil jwtUtil;

    public MemberService(MemberRepository memberRepository,
        BCryptPasswordEncoder bCryptPasswordEncoder,
        JwtUtil jwtUtil) {
        this.memberRepository = memberRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public SignupResponseDto signup(SignupRequestDto dto){
        // 이메일 중복 확인
        memberRepository.findByEmail(String.valueOf(dto.getEmail())).ifPresent(user1 -> {
            throw new IllegalArgumentException("이미 등록된 이메일입니다.");
        });

        // 비밀번호 암호화
        String encodedPassword = (bCryptPasswordEncoder.encode(dto.getPassword()));

        // member entity로 변환
        Member member = Member.builder()
            .email(dto.getEmail())
            .username(dto.getUsername())
            .password(encodedPassword)
            .provider(AuthProvider.EMAIL)
            .build();

        // profile entity로 변환
        Profile profile = Profile.builder()
            .height(dto.getHeight())
            .weight(dto.getWeight())
            .shoeSize(dto.getShoeSize())
            .member(member)  // 연관 관계 주입
            .build();

        member.setProfile(profile);

        // 저장
        Member saved = memberRepository.save(member);

        // 응답 DTO 변환
        return SignupResponseDto.from(saved);
    }

    public SigninResponseDto signin(SigninRequestDto dto){
        // 이메일로 사용자 조회
        Member member = memberRepository.findByEmail(dto.getEmail())
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 비밀번호 일치 여부 확인
        if (!bCryptPasswordEncoder.matches(dto.getPassword(), member.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        String token = jwtUtil.createJwt(member.getEmail(), member.getRole().name(), 60 * 60 * 1000L);
        return new SigninResponseDto(member.getUsername(), member.getEmail(), token);
    }
}
