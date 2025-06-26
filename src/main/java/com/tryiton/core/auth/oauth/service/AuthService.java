package com.tryiton.core.auth.oauth.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import org.springframework.beans.factory.annotation.Value;
import com.tryiton.core.auth.jwt.JwtUtil;
import com.tryiton.core.auth.oauth.dto.GoogleInfoDto;
import com.tryiton.core.common.enums.AuthProvider;
import com.tryiton.core.common.enums.Style;
import com.tryiton.core.common.enums.UserRole;
import com.tryiton.core.common.exception.BusinessException;
import com.tryiton.core.auth.oauth.dto.GoogleSigninRequestDto;
import com.tryiton.core.auth.oauth.dto.GoogleSignupRequestDto;
import com.tryiton.core.auth.oauth.dto.GoogleSignupResponseDto;
import com.tryiton.core.member.dto.SigninResponseDto;
import com.tryiton.core.member.entity.Member;
import com.tryiton.core.member.entity.Profile;
import com.tryiton.core.member.repository.MemberRepository;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AuthService {

    private final MemberRepository memberRepository;
    private final JwtUtil jwtUtil;
    private final String clientId;

    public AuthService(MemberRepository memberRepository, JwtUtil jwtUtil, 
                      @Value("${spring.security.oauth2.client.registration.google.client-id}") String clientId) {
        this.memberRepository = memberRepository;
        this.jwtUtil = jwtUtil;
        this.clientId = clientId;
    }

    // Google OAuth 토큰을 검증하고 사용자 정보를 반환합니다.
    public GoogleInfoDto authenticate(String token) {
        return extractUserInfoFromToken(token);
    }

    // 토큰에서 Google 사용자 정보를 추출합니다.
    private GoogleInfoDto extractUserInfoFromToken(String token) {
        try {
            log.info("Verifying Google ID token");
            log.info("Client ID: {}", clientId);
            
            if (clientId == null || clientId.trim().isEmpty()) {
                throw new BusinessException("Google Client ID가 설정되지 않았습니다.");
            }
            
            GoogleIdTokenVerifier verifier = createGoogleIdTokenVerifier();
            // 토큰 검증
            GoogleIdToken idToken = verifier.verify(token);
            if (idToken == null) {
                throw new BusinessException("유효하지 않은 ID 토큰입니다.");
            }
            Payload payload = idToken.getPayload();
            log.info("Token verified successfully for email: {}", payload.getEmail());
            
            // Payload로부터 사용자 정보 추출
            return convertPayloadToGoogleInfoDto(payload);

        } catch (GeneralSecurityException e) {
            log.error("Google 토큰 검증 실패 - Security Exception: {}", e.getMessage());
            throw new BusinessException("Google 토큰 검증 실패: " + e.getMessage());
        } catch (IOException e) {
            log.error("Google 토큰 검증 실패 - IO Exception: {}", e.getMessage());
            throw new BusinessException("Google 토큰 검증 실패: " + e.getMessage());
        } catch (Exception e) {
            log.error("Google 토큰 검증 중 예상치 못한 오류: {}", e.getMessage());
            throw new BusinessException("Google 토큰 검증 실패: " + e.getMessage());
        }
    }

    // Payload를 GoogleInfoDto로 변환합니다.
    private GoogleInfoDto convertPayloadToGoogleInfoDto(Payload payload) {
        String email = payload.getEmail();
        String pictureUrl = payload.containsKey("picture") ? (String) payload.get("picture") : null;
        return new GoogleInfoDto(email, pictureUrl);
    }

    // Google ID 토큰 검증기를 생성합니다.
    private GoogleIdTokenVerifier createGoogleIdTokenVerifier() {
        return new GoogleIdTokenVerifier.Builder(
            new NetHttpTransport(), new JacksonFactory())
            .setAudience(Collections.singletonList(clientId))
            .build();
    }

    public SigninResponseDto loginWithGoogle(GoogleSigninRequestDto dto) {
        GoogleInfoDto googleInfo = authenticate(dto.getIdToken());
        String email = googleInfo.getEmail();

        Member member = memberRepository.findByEmail(email)
            .orElseThrow(() -> new BusinessException("가입되지 않은 회원입니다. 회원가입이 필요합니다."));

        String jwt = jwtUtil.createJwt(email, member.getRole().name(), 60 * 60 * 1000L);

        return new SigninResponseDto(member.getUsername(), member.getEmail(), jwt);
    }

    public GoogleSignupResponseDto signupWithGoogle(GoogleSignupRequestDto dto) {
        try {
            log.info("Starting Google signup process for user: {}", dto.getUsername());
            
            GoogleInfoDto googleInfo = authenticate(dto.getIdToken());
            String email = googleInfo.getEmail();
            
            log.info("Google authentication successful for email: {}", email);

            // 이메일 중복 체크
            memberRepository.findByEmail(email).ifPresent(m -> {
                throw new BusinessException("이미 가입된 회원입니다.");
            });

            Member member = new Member();
            member.setEmail(email);
            member.setUsername(dto.getUsername());
            member.setBirthDate(dto.getBirthDate());
            member.setGender(dto.getGender());
            member.setPhoneNum(dto.getPhoneNum());
            member.setProvider(AuthProvider.GOOGLE);
            member.setRole(UserRole.USER);

            Profile profile = new Profile();
            profile.setHeight(dto.getHeight());
            profile.setWeight(dto.getWeight());
            profile.setShoeSize(dto.getShoeSize());
            profile.setPreferredStyle(Style.valueOf(dto.getPreferredStyle()));
            profile.setProfileImageUrl(googleInfo.getPictureUrl());

            member.setProfile(profile);
            profile.setMember(member);

            log.info("Saving member to database");
            Member saved = memberRepository.save(member);
            log.info("Member saved successfully with ID: {}", saved.getId());

            String jwt = jwtUtil.createJwt(email, member.getRole().name(), 60 * 60 * 1000L);
            return GoogleSignupResponseDto.from(saved, jwt);
            
        } catch (BusinessException e) {
            log.error("Business exception during signup: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during signup: {}", e.getMessage(), e);
            throw new BusinessException("회원가입 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}