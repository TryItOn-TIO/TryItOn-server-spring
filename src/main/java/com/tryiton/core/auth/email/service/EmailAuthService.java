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
import com.tryiton.core.auth.email.util.Validator;
import com.tryiton.core.auth.jwt.JwtUtil;
import com.tryiton.core.cart.entity.Cart;
import com.tryiton.core.cart.repository.CartRepository;
import com.tryiton.core.common.enums.AuthProvider;
import com.tryiton.core.common.enums.Gender;
import com.tryiton.core.common.enums.Style;
import com.tryiton.core.common.enums.UserRole;
import com.tryiton.core.common.exception.BusinessException;
import com.tryiton.core.member.entity.Member;
import com.tryiton.core.member.entity.Profile;
import com.tryiton.core.member.repository.MemberRepository;
import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailAuthService {

    private final MemberRepository memberRepository;
    private final CartRepository cartRepository;
    private final EmailVerificationRepository emailVerificationRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;

    public EmailAuthService(MemberRepository memberRepository, CartRepository cartRepository,
        EmailVerificationRepository emailVerificationRepository,
        BCryptPasswordEncoder bCryptPasswordEncoder, JwtUtil jwtUtil, EmailService emailService) {
        this.memberRepository = memberRepository;
        this.cartRepository = cartRepository;
        this.emailVerificationRepository = emailVerificationRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.jwtUtil = jwtUtil;
        this.emailService = emailService;
    }

    public void sendAuthenticationCode(EmailRequestDto dto) throws MessagingException {
        String email = dto.getEmail();

        // 이메일 유효성 검사
        Validator.validateEmail(email);

        // 이메일 중복 확인
        memberRepository.findByEmail(String.valueOf(dto.getEmail())).ifPresent(user1 -> {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "이미 등록된 이메일입니다.");
        });

        // 인증 코드 생성
        String code = RandomCodeGenerator.generateCode(6);

        // 인증 코드 저장
        EmailVerification ev = EmailVerification.create(dto.getEmail(), code);
        emailVerificationRepository.save(ev);

        // 메일 발송
        try {
            emailService.sendAuthenticationCode(email, code);
        } catch (Exception e) {
            log.error("이메일 인증 코드 발송 실패", e);
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "이메일 발송 중 오류가 발생했습니다.");
        }

        log.info("인증 코드 전송 완료: {} -> {}", email, code);
    }

    public Boolean verifyAuthenticationCode(EmailVerifyRequestDto dto){
        // 이메일 인증 요청을 했는 지 확인
        EmailVerification ev = emailVerificationRepository.findById(dto.getEmail())
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "인증 요청 내역이 없습니다."));

        // 이메일 인증 요청의 만료 여부 확인
        if (ev.isExpired()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "인증 코드가 만료되었습니다.");
        }

        // 인증 번호가 일치하는 지 확인 후, 해당 이메일 인증 완료 처리
        ev.verify(dto.getCode());
        emailVerificationRepository.save(ev); // verified 상태 변경 저장

        return true;
    }

    public EmailSignupResponseDto signupWithEmail(EmailSignupRequestDto dto){
        // 이메일 인증 완료 여부 확인
        EmailVerification ev = emailVerificationRepository.findById(dto.getEmail())
            .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, "이메일 인증이 필요합니다."));

        if (!ev.isVerified()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "이메일 인증이 완료되지 않았습니다.");
        }

        // 유효성 검증
        Validator.validatePassword(dto.getPassword());
        Validator.validateNickname(dto.getUsername());
        Validator.validatePhone(dto.getPhoneNum());

        // 비밀번호 암호화
        String encodedPassword = (bCryptPasswordEncoder.encode(dto.getPassword()));

        // member entity로 변환
        Member member = Member.builder()
            .email(dto.getEmail())
            .username(dto.getUsername())
            .password(encodedPassword)
            .birthDate(dto.getBirthDate())
            .gender(Gender.valueOf(dto.getGender()))
            .phoneNum(dto.getPhoneNum())
            .provider(AuthProvider.EMAIL)
            .role(UserRole.USER)
            .build();

        // profile entity로 변환
        Profile profile = Profile.builder()
            .preferredStyle(Style.valueOf(dto.getPreferredStyle()))
            .height(dto.getHeight())
            .weight(dto.getWeight())
            .shoeSize(dto.getShoeSize())
            .profileImageUrl(dto.getProfileImageUrl())
            .avatarBaseImageUrl(dto.getAvatarBaseImageUrl())
            .userBaseImageUrl(dto.getUserBaseImageUrl())
            .member(member)
            .build();

        member.setProfile(profile);

        // 저장
        Member saved = memberRepository.save(member);

        // Cart를 자동으로 생성
        Cart cart = new Cart(saved);
        cartRepository.save(cart);

        // 인증 정보 삭제
        emailVerificationRepository.delete(ev);

        // 응답 DTO 변환
        return EmailSignupResponseDto.from(saved);
    }

    public EmailSigninResponseDto signinWithEmail(EmailSigninRequestDto dto){
        // 이메일 유효성 검사
        Validator.validateEmail(dto.getEmail());

        // 이메일로 사용자 조회
        Member member = memberRepository.findByEmail(dto.getEmail())
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "가입되지 않았거나 비밀번호가 올바르지 않습니다."));

        // 비밀번호 일치 여부 확인
        if (!bCryptPasswordEncoder.matches(dto.getPassword(), member.getPassword())) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "가입되지 않았거나 비밀번호가 올바르지 않습니다.");
        }

        String token = jwtUtil.createJwt(member.getEmail(), member.getRole().name(), 60 * 60 * 1000L);
        return (EmailSigninResponseDto) EmailSigninResponseDto.builder()
            .username(member.getUsername())
            .email(member.getEmail())
            .accessToken(token)
            .build();
    }
}
