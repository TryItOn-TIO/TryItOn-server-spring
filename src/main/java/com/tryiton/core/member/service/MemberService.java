package com.tryiton.core.member.service;

import com.tryiton.core.member.entity.Member;
import com.tryiton.core.member.repository.MemberRepository;
import com.tryiton.core.mypage.dto.UserProfileResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));
        
        return new UserProfileResponse(
                member.getEmail(),
                member.getUsername(),
                member.getProfile() != null ? member.getProfile().getHeight() : null,
                member.getProfile() != null ? member.getProfile().getWeight() : null,
                member.getProfile() != null ? member.getProfile().getShoeSize() : null,
                member.getProvider()
        );
    }
}
