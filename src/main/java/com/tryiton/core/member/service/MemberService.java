package com.tryiton.core.member.service;

import com.tryiton.core.member.dto.request.PasswordChangeRequest;
import com.tryiton.core.member.dto.response.PasswordChangeResponse;
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
}
