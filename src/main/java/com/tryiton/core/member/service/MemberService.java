package com.tryiton.core.member.service;

import com.tryiton.core.common.enums.AuthProvider;
import com.tryiton.core.member.dto.GoogleSignupRequestDto;
import com.tryiton.core.member.dto.SigninRequestDto;
import com.tryiton.core.member.dto.SigninResponseDto;
import com.tryiton.core.member.dto.SignupRequestDto;
import com.tryiton.core.member.dto.SignupResponseDto;
import com.tryiton.core.member.entity.Member;
import com.tryiton.core.member.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    public SignupResponseDto signup(SignupRequestDto dto){
        // 이메일 중복 확인
        memberRepository.findByEmail(String.valueOf(dto.getEmail())).ifPresent(user1 -> {
            throw new IllegalArgumentException("이미 등록된 이메일입니다.");
        });

        // 비밀번호 암호화
        String encodedPassword = (bCryptPasswordEncoder.encode(dto.getPassword()));

        // entity로 변환
        Member member = new Member();
        member.setEmail(dto.getEmail());
        member.setPassword(encodedPassword);
        member.setProvider(AuthProvider.EMAIL);
        member.setNickname(dto.getNickname());
        member.setPreferredStyle(dto.getPreferredStyle());
        member.setHeight(dto.getHeight());
        member.setWeight(dto.getWeight());
        member.setAge(dto.getAge());

        // 저장
        Member saved = memberRepository.save(member);

        // 응답 DTO 변환
        return SignupResponseDto.from(saved);
    }

    public SignupResponseDto completeGoogleSignup(String email, GoogleSignupRequestDto dto) {
        Member member = memberRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("유저를 찾을 수 없습니다"));

        if (member.getProvider() != AuthProvider.GOOGLE) {
            throw new IllegalArgumentException("Google 계정이 아닙니다.");
        }

        // entity로 변환
        member.setNickname(dto.getNickname());
        member.setPreferredStyle(dto.getPreferredStyle());
        member.setHeight(dto.getHeight());
        member.setWeight(dto.getWeight());
        member.setAge(dto.getAge());

        // 저장
        memberRepository.save(member);

        // 응답 DTO 변환
        return SignupResponseDto.from(member);
    }

    public SigninResponseDto getMember(SigninRequestDto dto){
        String email = dto.getEmail();
        String password = dto.getPassword();

        // 이메일로 사용자 조회
        Member member = memberRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 비밀번호 일치 여부 확인
        if (!bCryptPasswordEncoder.matches(password, member.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // 로그인 성공 시 응답 DTO 반환
        return SigninResponseDto.from(member);
    }
}
