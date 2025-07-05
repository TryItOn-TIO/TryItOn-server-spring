package com.tryiton.core.mypage.service;

import com.tryiton.core.member.entity.Member;
import com.tryiton.core.member.entity.Profile;
import com.tryiton.core.member.repository.MemberRepository;
import com.tryiton.core.member.repository.ProfileRepository;
import com.tryiton.core.mypage.dto.*;
import com.tryiton.core.order.repository.OrderRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MyPageService {

    private final MemberRepository memberRepository;
    private final ProfileRepository profileRepository;
    private final OrderRepository orderRepository;

    @Transactional(readOnly = true)
    public ProfileResponseDto getProfile(Long userId) {
        Member member = findMemberById(userId);
        Profile profile = profileRepository.findByMember(member).orElse(null);
        return new ProfileResponseDto(member, profile);
    }

    @Transactional
    public void updateProfile(Long userId, ProfileUpdateRequestDto requestDto) {
        Member member = findMemberById(userId);
        
        // Member 정보 업데이트
        member.updateUsername(requestDto.getUsername());
        
        // 새로 생성하지 말고 기존 Profile을 반드시 찾기 (회원가입 시 이미 생성됨)
        Profile profile = profileRepository.findByMember(member)
                .orElseThrow(() -> new EntityNotFoundException("프로필을 찾을 수 없습니다."));
        
        // Profile 정보 업데이트
        profile.updateProfile(requestDto.getHeight(), requestDto.getWeight(), requestDto.getShoeSize());
        
        // 변경사항 저장 (더티 체킹으로 자동 저장되지만 명시적으로 호출)
        memberRepository.save(member);
        profileRepository.save(profile);
    }

    @Transactional(readOnly = true)
    public Page<OrderHistoryDto> getOrderHistory(Long userId, Pageable pageable) {
        return orderRepository.findOrderHistoryByUserId(userId, pageable).map(OrderHistoryDto::new);
    }

    private Member findMemberById(Long userId) {
        return memberRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));
    }
}