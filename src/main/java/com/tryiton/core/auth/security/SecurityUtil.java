package com.tryiton.core.auth.security;

import com.tryiton.core.common.exception.BusinessException;
import com.tryiton.core.member.entity.Member;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtil {

    /**
     * 현재 인증된 사용자의 Member 객체를 반환합니다.
     * @return 현재 인증된 사용자의 Member 객체
     * @throws BusinessException 인증되지 않은 사용자인 경우
     */
    public static Member getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "인증이 필요합니다.");
        }
        
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof CustomUserDetails)) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "유효하지 않은 인증 정보입니다.");
        }
        
        return ((CustomUserDetails) principal).getUser();
    }

    /**
     * 현재 인증된 사용자의 ID를 반환합니다.
     * @return 현재 인증된 사용자의 ID
     */
    public static Long getCurrentUserId() {
        return getCurrentUser().getId();
    }
}
