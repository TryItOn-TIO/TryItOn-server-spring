package com.tryiton.core.auth.email.service;

import com.tryiton.core.auth.email.dto.EmailRequestDto;
import com.tryiton.core.auth.email.dto.EmailSigninRequestDto;
import com.tryiton.core.auth.email.dto.EmailSigninResponseDto;
import com.tryiton.core.auth.email.dto.EmailSignupRequestDto;
import com.tryiton.core.auth.email.dto.EmailSignupResponseDto;
import com.tryiton.core.auth.email.dto.EmailVerifyRequestDto;
import com.tryiton.core.auth.email.entity.EmailVerification;
import com.tryiton.core.auth.email.repository.EmailVerificationRepository;
import com.tryiton.core.auth.email.util.RandomCodeGenerator;
import com.tryiton.core.auth.jwt.JwtUtil;
import com.tryiton.core.common.enums.AuthProvider;
import com.tryiton.core.common.enums.Style;
import com.tryiton.core.common.enums.UserRole;
import com.tryiton.core.member.entity.Member;
import com.tryiton.core.member.entity.Profile;
import com.tryiton.core.member.repository.MemberRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailAuthService {

    private final MemberRepository memberRepository;
    private final EmailVerificationRepository emailVerificationRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final JwtUtil jwtUtil;
    private final JavaMailSender mailSender;

    public EmailAuthService(MemberRepository memberRepository,
        EmailVerificationRepository emailVerificationRepository,
        BCryptPasswordEncoder bCryptPasswordEncoder, JwtUtil jwtUtil, JavaMailSender mailSender) {
        this.memberRepository = memberRepository;
        this.emailVerificationRepository = emailVerificationRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.jwtUtil = jwtUtil;
        this.mailSender = mailSender;
    }

    public void sendAuthenticationCode(EmailRequestDto dto){
        String email = dto.getEmail();

        // 이메일 중복 확인
        memberRepository.findByEmail(String.valueOf(dto.getEmail())).ifPresent(user1 -> {
            throw new IllegalArgumentException("이미 등록된 이메일입니다.");
        });

        // 인증 코드 생성
        String code = RandomCodeGenerator.generateCode(6);

        // 인증 코드 저장
        EmailVerification ev = EmailVerification.create(dto.getEmail(), code);
        emailVerificationRepository.save(ev);

        // 메일 발송
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("[TryItOn] 이메일 인증 코드");
        message.setText("인증 코드: " + code);
        message.setFrom("tryiton486@gmail.com");

        mailSender.send(message);
        log.info("인증 코드 전송 완료: {} -> {}", email, code);
    }

    public Boolean verifyAuthenticationCode(EmailVerifyRequestDto dto){
        // 이메일 인증 요청을 했는 지 확인
        EmailVerification ev = emailVerificationRepository.findById(dto.getEmail())
            .orElseThrow(() -> new IllegalArgumentException("인증 요청 내역이 없습니다."));

        // 이메일 인증 요청의 만료 여부 확인
        if (ev.isExpired()) {
            throw new IllegalArgumentException("인증 코드가 만료되었습니다.");
        }

        // 인증 번호가 일치하는 지 확인 후, 해당 이메일 인증 완료 처리
        ev.verify(dto.getCode());
        emailVerificationRepository.save(ev); // verified 상태 변경 저장

        return true;
    }

    public EmailSignupResponseDto signupWithEmail(EmailSignupRequestDto dto){
        // 이메일 인증 완료 여부 확인
        EmailVerification ev = emailVerificationRepository.findById(dto.getEmail())
            .orElseThrow(() -> new IllegalArgumentException("이메일 인증이 필요합니다."));

        if (!ev.isVerified()) {
            throw new IllegalArgumentException("이메일 인증이 완료되지 않았습니다.");
        }

        // 비밀번호 암호화
        String encodedPassword = (bCryptPasswordEncoder.encode(dto.getPassword()));

        // member entity로 변환
        Member member = Member.builder()
            .email(dto.getEmail())
            .username(dto.getUsername())
            .password(encodedPassword)
            .birthDate(dto.getBirthDate())
            .gender(dto.getGender())
            .phoneNum(dto.getPhoneNum())
            .provider(AuthProvider.EMAIL)
            .role(UserRole.USER)
            .build();

        // profile entity로 변환
        Profile profile = Profile.builder()
            .height(dto.getHeight())
            .weight(dto.getWeight())
            .shoeSize(dto.getShoeSize())
            .preferredStyle(Style.valueOf(dto.getPreferredStyle()))
            .profileImageUrl(dto.getProfileImageUrl())
            .member(member)
            .build();

        member.setProfile(profile);

        // 저장
        Member saved = memberRepository.save(member);

        // 인증 정보 삭제
        emailVerificationRepository.delete(ev);

        // 응답 DTO 변환
        return EmailSignupResponseDto.from(saved);
    }

    public EmailSigninResponseDto signinWithEmail(EmailSigninRequestDto dto){
        // 이메일로 사용자 조회
        Member member = memberRepository.findByEmail(dto.getEmail())
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 비밀번호 일치 여부 확인
        if (!bCryptPasswordEncoder.matches(dto.getPassword(), member.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        String token = jwtUtil.createJwt(member.getEmail(), member.getRole().name(), 60 * 60 * 1000L);
        return new EmailSigninResponseDto(member.getUsername(), member.getEmail(), token);
    }
}
