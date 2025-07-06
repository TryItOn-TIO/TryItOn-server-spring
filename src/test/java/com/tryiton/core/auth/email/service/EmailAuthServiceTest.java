package com.tryiton.core.auth.email.service;

import com.tryiton.core.auth.email.dto.EmailSignupRequestDto;
import com.tryiton.core.auth.email.entity.EmailVerification;
import com.tryiton.core.auth.email.repository.EmailVerificationRepository;
import com.tryiton.core.auth.jwt.JwtUtil;
import com.tryiton.core.avatar.dto.request.AvatarCreateRequest;
import com.tryiton.core.avatar.service.AvatarService;
import com.tryiton.core.cart.repository.CartRepository;
import com.tryiton.core.member.entity.Member;
import com.tryiton.core.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailAuthServiceTest {

    @InjectMocks
    private EmailAuthService emailAuthService;

    @Mock
    private MemberRepository memberRepository;
    @Mock
    private CartRepository cartRepository;
    @Mock
    private EmailVerificationRepository emailVerificationRepository;
    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private EmailService emailService;
    @Mock
    private AvatarService avatarService; // 아바타 서비스 Mock

    @Test
    @DisplayName("회원가입 성공 시, 아바타 생성 서비스가 올바르게 호출되어야 한다")
    void signupWithEmail_ShouldCallCreateAvatarService() {
        // --- Arrange (Given) ---
        // 1. 회원가입 요청 DTO 생성
        String avatarBaseUrl = "http://s3.avatar-base.com/image.jpg";
        EmailSignupRequestDto requestDto = EmailSignupRequestDto.builder()
            .email("test@example.com")
            .password("password123!")
            .username("테스트유저")
            .birthDate(LocalDate.of(2000, 1, 1))
            .gender("M")
            .phoneNum("010-1234-5678")
            .preferredStyle("CASUAL")
            .height(180)
            .weight(75)
            .shoeSize(270)
            .avatarBaseImageUrl(avatarBaseUrl)
            .build();

        // 2. 이메일 인증이 완료된 상태로 Mocking
        EmailVerification verifiedEmail = EmailVerification.create(requestDto.getEmail(), "123456");
        verifiedEmail.verify("123456"); // 인증 완료 처리
        when(emailVerificationRepository.findById(requestDto.getEmail())).thenReturn(Optional.of(verifiedEmail));

        // 3. Member 객체 저장을 Mocking
        Member member = Member.builder().id(1L).email(requestDto.getEmail()).build();
        when(memberRepository.save(any(Member.class))).thenReturn(member);

        // --- Act (When) ---
        // 4. 서비스 메소드 호출
        emailAuthService.signupWithEmail(requestDto);

        // --- Assert (Then) ---
        // 5. avatarService.create 메소드가 정확히 1번 호출되었는지 검증
        verify(avatarService).create(any(Member.class), any(AvatarCreateRequest.class));

        // 6. avatarService.create에 전달된 인자들을 캡처하여 검증
        ArgumentCaptor<Member> memberCaptor = ArgumentCaptor.forClass(Member.class);
        ArgumentCaptor<AvatarCreateRequest> requestCaptor = ArgumentCaptor.forClass(AvatarCreateRequest.class);

        verify(avatarService).create(memberCaptor.capture(), requestCaptor.capture());

        Member capturedMember = memberCaptor.getValue();
        AvatarCreateRequest capturedRequest = requestCaptor.getValue();

        // 6-1. 전달된 Member 객체가 올바른지 확인
        assertThat(capturedMember.getId()).isEqualTo(1L);
        assertThat(capturedMember.getEmail()).isEqualTo("test@example.com");

        // 6-2. 전달된 AvatarCreateRequest 객체가 올바른지 확인
        assertThat(capturedRequest.getUserId()).isEqualTo("1");
        assertThat(capturedRequest.getTryOnImgUrl()).isEqualTo(avatarBaseUrl);
    }
}