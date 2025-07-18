package com.tryiton.core.member.service;

import com.tryiton.core.member.dto.request.PasswordChangeRequest;
import com.tryiton.core.member.dto.request.WithdrawRequest;
import com.tryiton.core.member.dto.response.PasswordChangeResponse;
import com.tryiton.core.member.dto.response.WithdrawResponse;
import com.tryiton.core.common.enums.AuthProvider;
import com.tryiton.core.member.entity.Member;
import com.tryiton.core.member.repository.MemberRepository;
import com.tryiton.core.mypage.dto.UserProfileResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));
        
        boolean canChangePassword = member.getProvider() == AuthProvider.EMAIL && member.getPassword() != null;
        
        return new UserProfileResponse(
                member.getEmail(),
                member.getUsername(),
                member.getProfile() != null ? member.getProfile().getHeight() : null,
                member.getProfile() != null ? member.getProfile().getWeight() : null,
                member.getProfile() != null ? member.getProfile().getShoeSize() : null,
                member.getProvider(),
                canChangePassword
        );
    }

    @Transactional
    public PasswordChangeResponse changePassword(Long memberId, PasswordChangeRequest request) {
        // 1. 회원 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));

        // 2. 소셜 로그인 사용자 체크
        if (member.getProvider() != AuthProvider.EMAIL) {
            return PasswordChangeResponse.failure("소셜 로그인 사용자는 비밀번호를 변경할 수 없습니다.");
        }

        // 3. 새 비밀번호 확인 체크
        if (!request.isNewPasswordMatched()) {
            return PasswordChangeResponse.failure("새 비밀번호와 확인 비밀번호가 일치하지 않습니다.");
        }

        // 4. 현재 비밀번호 확인
        if (!passwordEncoder.matches(request.getCurrentPassword(), member.getPassword())) {
            return PasswordChangeResponse.failure("현재 비밀번호가 올바르지 않습니다.");
        }

        // 5. 새 비밀번호가 현재 비밀번호와 같은지 확인
        if (passwordEncoder.matches(request.getNewPassword(), member.getPassword())) {
            return PasswordChangeResponse.failure("새 비밀번호는 현재 비밀번호와 달라야 합니다.");
        }

        // 6. 비밀번호 변경
        String encodedNewPassword = passwordEncoder.encode(request.getNewPassword());
        member.changePassword(encodedNewPassword);

        return PasswordChangeResponse.success();
    }

    @Transactional(readOnly = true)
    public boolean canChangePassword(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));
        
        return member.getProvider() == AuthProvider.EMAIL && member.getPassword() != null;
    }
    
    /**
     * 회원 탈퇴 처리
     * @param memberId 회원 ID
     * @param request 탈퇴 요청 정보 (비밀번호 확인 등)
     * @return 탈퇴 처리 결과
     */
    @Transactional
    public WithdrawResponse withdrawMember(Long memberId, WithdrawRequest request) {
        // 1. 회원 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));
        
        // 2. 이미 탈퇴한 회원인지 확인
        if (member.isWithdraw()) {
            return WithdrawResponse.failure("이미 탈퇴한 회원입니다.");
        }
        
        // 3. 비밀번호 확인 (이메일 로그인 사용자인 경우)
        if (member.getProvider() == AuthProvider.EMAIL) {
            if (request.getPassword() == null || !passwordEncoder.matches(request.getPassword(), member.getPassword())) {
                return WithdrawResponse.failure("비밀번호가 일치하지 않습니다.");
            }
        }
        
        // 4. 회원 탈퇴 처리 (withdraw 필드를 true로 설정)
        member.setWithdraw(true);
        
        // 5. 개인정보 마스킹 처리 (GDPR, 개인정보보호법 등 규정 준수)
        anonymizeMemberData(member);
        
        return WithdrawResponse.success();
    }
    
    /**
     * 탈퇴 회원의 개인정보 익명화 처리
     * @param member 탈퇴할 회원
     */
    private void anonymizeMemberData(Member member) {
        // 이메일 익명화 (원본 이메일 형식 유지하면서 마스킹)
        String emailParts[] = member.getEmail().split("@");
        String maskedEmail = "withdrawn_" + member.getId() + "@" + 
                (emailParts.length > 1 ? emailParts[1] : "anonymous.com");
        member.setEmail(maskedEmail);
        
        // 사용자명 익명화
        member.setUsername("탈퇴회원");
        
        // 전화번호 익명화
        member.setPhoneNum("000-0000-0000");
        
        // 비밀번호 무효화 (로그인 불가능하게)
        if (member.getPassword() != null) {
            member.setPassword(passwordEncoder.encode(java.util.UUID.randomUUID().toString()));
        }
    }
}
