package com.tryiton.core.auth.jwt.service;

import com.tryiton.core.auth.jwt.CustomUserDetails;
import com.tryiton.core.member.repository.MemberRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailService  implements UserDetailsService {
    public final MemberRepository userRepository;

    public CustomUserDetailService(MemberRepository memberRepository) {
        this.userRepository = memberRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        return userRepository.findByEmail(email)
            .map(CustomUserDetails::new)
            .orElseThrow(() -> new UsernameNotFoundException("해당 이메일을 가진 사용자가 없습니다: " + email));
    }
}